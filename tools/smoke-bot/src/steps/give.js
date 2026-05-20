'use strict';
const { awaitState, sleep } = require('../utils/await');

// Usage: { give: 'hbm:deco_crt 1 2' }
// Resolves registry name → short name for inventory lookup.
module.exports = async (ctx, spec) => {
  const parts    = spec.split(/\s+/);
  const itemFull = parts[0];
  // Inventory lookup uses the short name (without namespace)
  const itemShort  = itemFull.includes(':') ? itemFull.split(':')[1] : itemFull;
  const expectMeta = parts.length >= 3 ? parseInt(parts[2], 10) : undefined;

  // Inventory protection — clear if full so give doesn't fail silently
  if (ctx.bot.inventory.emptySlotCount() === 0) {
    ctx.bot.chat('/clear @s');
    await sleep(300);
  }

  ctx.bot.chat(`/give @s ${spec}`);

  // Poll until item appears (handles server packet delay)
  await awaitState(() => {
    const items = ctx.bot.inventory.items();
    return items.find(it =>
      it.name === itemShort &&
      (expectMeta === undefined || it.metadata === expectMeta)
    );
  }, { timeoutMs: 3000, label: `give ${spec}` });
};
