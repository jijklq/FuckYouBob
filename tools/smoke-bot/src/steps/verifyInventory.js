'use strict';
const { retry } = require('../utils/await');

class AssertionError extends Error {
  constructor(msg) { super(msg); this.name = 'AssertionError'; this.code = 'ASSERTION'; }
}

// Usage: { verifyInventory: { item: 'stone', meta: 0, count: 1 } }
module.exports = async (ctx, expect) => {
  await retry(async () => {
    const items = ctx.bot.inventory.items();
    const found = items.find(it =>
      it.name === expect.item &&
      (expect.meta  === undefined || it.metadata === expect.meta) &&
      (expect.count === undefined || it.count    >= expect.count)
    );
    if (!found) {
      const dump = items.map(it => `${it.name}:${it.metadata}x${it.count}`).join(', ') || '(empty)';
      throw new AssertionError(
        `verifyInventory: ${JSON.stringify(expect)} not found. Have: [${dump}]`
      );
    }
  }, { attempts: 3, initialDelay: 300, label: 'verifyInventory' });
};
