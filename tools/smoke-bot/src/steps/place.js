'use strict';
const { resolvePos } = require('../utils/positions');
const { awaitState }  = require('../utils/await');
const Vec3 = require('vec3');

// Usage: { place: { pos: '~ ~ ~+1', block: 'minecraft:stone', meta: 0 } }
// OR shorthand: { place: '~ ~+1 ~' } — uses currently-held hotbar item
module.exports = async (ctx, arg) => {
  let pos, blockName, blockMeta;

  if (typeof arg === 'string') {
    pos = resolvePos(arg, ctx.anchor);
    const held = ctx.bot.inventory.slots[ctx.bot.quickBarSlot + 36];
    if (!held) throw new Error('place: hotbar empty and no block specified');
    // Try to recover full registry name; assume minecraft: namespace as fallback
    blockName = held.name.includes(':') ? held.name : `minecraft:${held.name}`;
    blockMeta = held.metadata || 0;
  } else {
    pos       = resolvePos(arg.pos, ctx.anchor);
    blockName = arg.block;
    blockMeta = arg.meta !== undefined ? arg.meta : 0;
  }

  ctx.bot.chat(`/setblock ${pos.x} ${pos.y} ${pos.z} ${blockName} ${blockMeta}`);

  await awaitState(() => {
    const b = ctx.bot.blockAt(new Vec3(pos.x, pos.y, pos.z));
    return b && b.name !== 'air';
  }, { timeoutMs: 3000, label: `place at ${pos.x},${pos.y},${pos.z}` });
};
