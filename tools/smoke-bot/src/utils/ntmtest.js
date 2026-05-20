'use strict';

const { sleep } = require('./await');

/**
 * Send `/ntmtest <cmd>` and collect all [NTMTEST] response messages
 * until 500ms of silence (response settled) or overall timeout.
 *
 * Returns Map<string, string> of parsed key=value pairs.
 * Throws if no [NTMTEST] response arrives within timeoutMs.
 *
 * Parsing rules:
 *   - Each sendMessage() call arrives as a separate 'message' event
 *   - Some lines carry multiple kv pairs: "slot=hand item=minecraft:stone count=2 meta=0"
 *   - Split on whitespace that precedes a `word=` pattern
 *   - Values may contain braces/commas (props={facing=east,variant=2}) → take everything up to next key
 */
async function awaitNtmtestResponse(bot, cmd, opts = {}) {
  const timeoutMs = opts.timeoutMs || 3000;
  const idleMs    = opts.idleMs    || 500;

  const results = new Map();
  let lastSeen  = 0;

  const listener = (jsonMsg) => {
    const text = typeof jsonMsg === 'string' ? jsonMsg : jsonMsg.toString();
    if (!text.startsWith('[NTMTEST]')) return;

    const rest = text.slice('[NTMTEST]'.length).trim();

    // Split on whitespace that precedes a key= pattern (word chars only before '=')
    const pairs = rest.split(/\s+(?=[a-zA-Z_]\w*=)/);

    for (const pair of pairs) {
      const eqIdx = pair.indexOf('=');
      if (eqIdx < 0) continue;
      const k = pair.slice(0, eqIdx).trim();
      const v = pair.slice(eqIdx + 1).trim();
      results.set(k, v);
    }

    lastSeen = Date.now();
  };

  bot.on('message', listener);
  try {
    bot.chat('/ntmtest ' + cmd);
    const start = Date.now();

    // Phase 1: wait for first response
    while (lastSeen === 0 && Date.now() - start < timeoutMs) {
      await sleep(50);
    }

    // Phase 2: wait for idle (no new messages for idleMs)
    while (lastSeen > 0 &&
           Date.now() - lastSeen < idleMs &&
           Date.now() - start < timeoutMs) {
      await sleep(50);
    }
  } finally {
    bot.removeListener('message', listener);
  }

  if (results.size === 0) {
    throw new Error(
      `awaitNtmtestResponse: no [NTMTEST] response to "${cmd}" within ${timeoutMs}ms`
    );
  }

  return results;
}

module.exports = { awaitNtmtestResponse };
