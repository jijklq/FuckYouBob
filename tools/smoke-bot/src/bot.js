'use strict';

const mineflayer = require('mineflayer');
const { forgeHandshake } = require('minecraft-protocol-forge');

const HOST = process.env.MC_HOST || 'localhost';
const PORT = parseInt(process.env.MC_PORT || '25565', 10);
const USERNAME = process.env.MC_USER || 'smokebot';
const VERSION = '1.12.2';

console.log(`[smoke-bot] connecting to ${HOST}:${PORT} as ${USERNAME} (MC ${VERSION})`);

const botOptions = {
  host: HOST,
  port: PORT,
  username: USERNAME,
  version: VERSION,
  auth: 'offline',
};

const bot = mineflayer.createBot(botOptions);

// Plan A.3: minecraft-protocol-forge — hooks into bot._client directly
// Declare Forge + NTM so the server accepts the client
forgeHandshake(bot._client, {
  forgeMods: [
    { modid: 'mcp',   version: '9.42' },
    { modid: 'FML',   version: '8.0.99.99' },
    { modid: 'Forge', version: '14.23.5.2860' },
    { modid: 'hbm',   version: '1.0.0' },
  ]
});
console.log('[smoke-bot] forge FML|HS handshake handler installed (hbm@1.0.0)');

let exitCode = 1;  // pessimistic — set 0 on full success

const TIMEOUT_MS = 30_000;
const hardTimer = setTimeout(() => {
  console.error('[smoke-bot] FATAL: 30s timeout, no spawn event');
  process.exit(2);
}, TIMEOUT_MS);

bot.on('login', () => {
  console.log('[smoke-bot] login event — TCP+protocol OK');
});

bot.on('spawn', () => {
  console.log(`[smoke-bot] spawn event — bot in world at ${bot.entity.position}`);
  bot.chat('hello from smoke-bot B.1');
  setTimeout(() => {
    console.log('[smoke-bot] disconnecting cleanly');
    exitCode = 0;
    bot.quit('test complete');
  }, 2000);
});

bot.on('kicked', (reason) => {
  console.error(`[smoke-bot] KICKED: ${reason}`);
});

bot.on('error', (err) => {
  console.error(`[smoke-bot] ERROR: ${err.message}`);
});

bot.on('end', (reason) => {
  clearTimeout(hardTimer);
  console.log(`[smoke-bot] disconnected: ${reason}`);
  process.exit(exitCode);
});
