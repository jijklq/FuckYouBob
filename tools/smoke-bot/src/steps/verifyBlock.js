'use strict';
const { resolvePos } = require('../utils/positions');
const { retry }       = require('../utils/await');
const Vec3 = require('vec3');

class AssertionError extends Error {
  constructor(msg) { super(msg); this.name = 'AssertionError'; this.code = 'ASSERTION'; }
}

// Usage: { verifyBlock: { pos: '~ ~ ~+1', name: 'stone', meta: 0 } }
module.exports = async (ctx, expect) => {
  const pos = resolvePos(expect.pos, ctx.anchor);

  await retry(async (attempt) => {
    const block = ctx.bot.blockAt(new Vec3(pos.x, pos.y, pos.z));
    if (!block) {
      throw new AssertionError(
        `verifyBlock: no block data at ${pos.x},${pos.y},${pos.z} (attempt ${attempt})`
      );
    }
    if (expect.name !== undefined && block.name !== expect.name) {
      throw new AssertionError(
        `verifyBlock: expected name="${expect.name}", got "${block.name}" at ${pos.x},${pos.y},${pos.z}`
      );
    }
    if (expect.meta !== undefined && block.metadata !== expect.meta) {
      throw new AssertionError(
        `verifyBlock: expected meta=${expect.meta}, got ${block.metadata} at ${pos.x},${pos.y},${pos.z}`
      );
    }
  }, { attempts: 3, initialDelay: 300, label: 'verifyBlock' });
};
