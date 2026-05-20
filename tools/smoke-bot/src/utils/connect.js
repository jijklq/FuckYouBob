'use strict';
const mineflayer = require('mineflayer');
const forge = require('minecraft-protocol-forge');

// B.1 confirmed: forgeHandshake with explicit modlist works; autoVersionForge does NOT
// (server kicks with modlist-drift when we declare empty list)
const DEFAULT_FORGE_MODS = [
  { modid: 'mcp',     version: '9.42' },
  { modid: 'FML',     version: '8.0.99.99' },
  { modid: 'Forge',   version: '14.23.5.2860' },
  { modid: 'hbm',     version: '1.0.0' },
  { modid: 'ntmtest', version: '1.0.0' },
];

function createBot(options = {}) {
  const bot = mineflayer.createBot({
    host:     options.host     || process.env.MC_HOST || 'localhost',
    port:     parseInt(options.port || process.env.MC_PORT || '25565', 10),
    username: options.username || process.env.MC_USER || 'smokebot',
    version:  '1.12.2',
    auth:     'offline',
  });
  // B.1 winning approach: explicit forgeMods handshake
  forge.forgeHandshake(bot._client, {
    forgeMods: options.forgeMods || DEFAULT_FORGE_MODS,
  });
  return bot;
}

// Modlist drift detection: kicked when server requires mods not in our list
const MODLIST_DRIFT_PATTERNS = [
  /mod.*not found/i,
  /missing mods/i,
  /This server requires/i,
  /Server Mod rejections/i,
];

function isModlistDrift(kickReason) {
  const text = JSON.stringify(kickReason);
  return MODLIST_DRIFT_PATTERNS.some(p => p.test(text));
}

module.exports = { createBot, isModlistDrift, DEFAULT_FORGE_MODS };
