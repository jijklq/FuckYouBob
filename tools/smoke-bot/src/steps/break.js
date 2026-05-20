'use strict';
const { resolvePos } = require('../utils/positions');
const { awaitState }  = require('../utils/await');
const Vec3 = require('vec3');

// Usage: { break: '~ ~ ~+1' }
module.exports = async (ctx, posSpec) => {
  const pos = resolvePos(posSpec, ctx.anchor);
  ctx.bot.chat(`/setblock ${pos.x} ${pos.y} ${pos.z} air 0 destroy`);

  await awaitState(() => {
    const b = ctx.bot.blockAt(new Vec3(pos.x, pos.y, pos.z));
    return b && b.name === 'air';
  }, { timeoutMs: 3000, label: `break at ${pos.x},${pos.y},${pos.z}` });
};
