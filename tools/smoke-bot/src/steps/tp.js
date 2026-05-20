'use strict';
const { resolvePos } = require('../utils/positions');
const { awaitState }  = require('../utils/await');

// Usage: { tp: '100 65 100' }  or  { tp: { x:100, y:65, z:100 } }
module.exports = async (ctx, posSpec) => {
  const pos = resolvePos(posSpec, ctx.anchor);
  ctx.bot.chat(`/tp @s ${pos.x} ${pos.y} ${pos.z}`);

  // Wait until entity position matches AND chunk is loaded (blockAt != null)
  await awaitState(() => {
    const p = ctx.bot.entity.position;
    const arrived = Math.abs(p.x - pos.x) < 2 && Math.abs(p.z - pos.z) < 2;
    return arrived && ctx.bot.blockAt(p) !== null;
  }, { timeoutMs: 5000, label: `tp + chunk-load ${pos.x},${pos.y},${pos.z}` });
};
