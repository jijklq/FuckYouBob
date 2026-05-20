'use strict';

const path = require('path');
const { createBot, isModlistDrift } = require('./utils/connect');
const { awaitState, sleep, TimeoutError } = require('./utils/await');
const { classify } = require('./utils/classify');

const DEFAULT_STEP_TIMEOUT_MS     = 15_000;
const DEFAULT_SCENARIO_TIMEOUT_MS = 3 * 60 * 1000;

// Determinism gamerules applied before every scenario
const DETERMINISM_SETUP = [
  '/gamerule doDaylightCycle false',
  '/gamerule doWeatherCycle false',
  '/gamerule doMobSpawning false',
  '/gamerule randomTickSpeed 0',
  '/gamerule keepInventory true',
  '/gamerule sendCommandFeedback true',
  '/time set day',
  '/weather clear',
];

async function runScenario(scenarioPath) {
  const scenario = require(path.resolve(scenarioPath));
  console.log(`[runner] === ${scenario.name} ===`);

  const bot = createBot();
  let modlistDrift = false;

  bot.on('kicked', (reason) => {
    if (isModlistDrift(reason)) {
      modlistDrift = true;
      console.error(`[runner] MODLIST DRIFT — update DEFAULT_FORGE_MODS in connect.js. Kick: ${JSON.stringify(reason)}`);
    } else {
      console.error(`[runner] KICKED: ${JSON.stringify(reason)}`);
    }
  });

  bot.on('error', (err) => {
    console.error(`[runner] bot error: ${err.message}`);
  });

  const result = { passed: 0, failed: 0, failures: [] };

  try {
    // Wait for spawn (chunk + entity ready)
    await awaitState(
      () => bot.entity !== undefined,
      { timeoutMs: 30000, label: 'spawn' }
    );
    console.log(`[runner] spawned at ${bot.entity.position}`);

    // Determinism setup — 300ms between commands to avoid disconnect.spam
    for (const cmd of DETERMINISM_SETUP) {
      bot.chat(cmd);
      await sleep(300);
    }

    // Scenario-specific setup commands
    if (Array.isArray(scenario.setup)) {
      for (const cmd of scenario.setup) {
        bot.chat(cmd);
        await sleep(300);
      }
    }

    // Arena teleport + chunk-load gate
    let anchor = { x: 0, y: 64, z: 0 };
    if (scenario.arena) {
      anchor = scenario.arena;
      bot.chat(`/tp @s ${anchor.x} ${anchor.y} ${anchor.z}`);
      // Must confirm BOTH arrival at target AND chunk loaded — entity position lags the tp packet
      await awaitState(() => {
        const p = bot.entity.position;
        const arrived = Math.abs(p.x - anchor.x) < 2 && Math.abs(p.z - anchor.z) < 2;
        return arrived && bot.blockAt(p) !== null;
      }, { timeoutMs: 10000, label: 'arena tp + chunk-load' });
      console.log(`[runner] arena at ${anchor.x},${anchor.y},${anchor.z} loaded`);
    }

    const ctx = { bot, anchor, scenario };
    const stepRegistry = require('./steps');
    const scenarioStart = Date.now();

    for (let i = 0; i < scenario.steps.length; i++) {
      // Global scenario timeout
      if (Date.now() - scenarioStart > DEFAULT_SCENARIO_TIMEOUT_MS) {
        result.failed++;
        result.failures.push({ i, type: 'timeout', error: 'scenario global timeout exceeded' });
        break;
      }

      const step = scenario.steps[i];
      // Find first key that matches a known step (skip meta-keys like _timeoutMs)
      const stepName = Object.keys(step).find(k => k !== '_timeoutMs' && stepRegistry[k]);

      if (!stepName) {
        const type = 'runtime';
        const error = `unknown step keys: ${Object.keys(step).join(', ')}`;
        console.error(`[runner] step ${i} FAIL [${type}]: ${error}`);
        result.failed++;
        result.failures.push({ i, type, error });
        if (scenario.stopOnFail) break;
        continue;
      }

      const stepTimeout = step._timeoutMs || DEFAULT_STEP_TIMEOUT_MS;

      try {
        await Promise.race([
          stepRegistry[stepName](ctx, step[stepName], step),
          sleep(stepTimeout).then(() => {
            throw new TimeoutError(`step "${stepName}" exceeded budget of ${stepTimeout}ms`);
          }),
        ]);
        console.log(`[runner] step ${i} ${stepName} OK`);
        result.passed++;
      } catch (err) {
        const type = classify(err);
        console.error(`[runner] step ${i} ${stepName} FAIL [${type}]: ${err.message}`);
        result.failed++;
        result.failures.push({ i, step: stepName, type, error: err.message });
        if (scenario.stopOnFail) break;
      }
    }
  } finally {
    // Cleanup ALWAYS runs — try/finally guarantees this on throw, kick, or normal exit
    if (scenario.cleanup && bot.entity) {
      try {
        bot.chat(scenario.cleanup);
        await sleep(800); // give server time to process fill/setblock
      } catch (e) { /* swallow — cleanup best-effort */ }
    }
    try { bot.quit('scenario complete'); } catch (e) {}
    await sleep(300);

    if (modlistDrift) {
      console.error('[runner] ABORT: modlist drift detected. Update DEFAULT_FORGE_MODS in connect.js to match server modlist.');
    }
  }

  return result;
}

async function main() {
  const scenarioArg = process.argv[2];
  if (!scenarioArg) {
    console.error('Usage: node src/scenario-runner.js <scenarios/name.js>');
    process.exit(2);
  }

  let result;
  try {
    result = await runScenario(scenarioArg);
  } catch (err) {
    console.error(`[runner] FATAL: ${err.stack}`);
    process.exit(2);
  }

  console.log(`[runner] === REPORT ===`);
  console.log(`[runner] passed=${result.passed} failed=${result.failed}`);

  if (result.failures.length) {
    const byType = {};
    for (const f of result.failures) byType[f.type] = (byType[f.type] || 0) + 1;
    console.log(`[runner] failure types: ${JSON.stringify(byType)}`);
    for (const f of result.failures) {
      console.log(`  - step ${f.i} [${f.type}] ${f.step || '?'}: ${f.error}`);
    }
  }

  process.exit(result.failed === 0 ? 0 : 1);
}

if (require.main === module) main();
module.exports = { runScenario };
