'use strict';

const DIRS = {
  north: { yaw: Math.PI,          pitch: 0 },
  south: { yaw: 0,                pitch: 0 },
  west:  { yaw:  Math.PI / 2,     pitch: 0 },
  east:  { yaw: -Math.PI / 2,     pitch: 0 },
  up:    { yaw: 0,                pitch: -Math.PI / 2 },
  down:  { yaw: 0,                pitch:  Math.PI / 2 },
};

// Usage: { lookAt: 'north' }
module.exports = async (ctx, dir) => {
  const d = DIRS[dir];
  if (!d) throw new Error(`lookAt: bad direction "${dir}". Valid: ${Object.keys(DIRS).join(', ')}`);
  await ctx.bot.look(d.yaw, d.pitch, true);
};
