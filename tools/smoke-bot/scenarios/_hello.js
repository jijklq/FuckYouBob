'use strict';

module.exports = {
  name: '_hello — dry-run всех 9 step-типов + resilience smoke',

  // Arena: flat open space, clear of other blocks
  arena: { x: 100, y: 65, z: 100 },

  // Scenario-specific setup (determinism gamerules are applied automatically by runner)
  setup: [
    '/gamemode 1 @s',
    '/effect @s minecraft:resistance 9999 4 true',
  ],

  // Cleanup: wipe arena column after test (runs even on failure)
  cleanup: '/fill 95 64 95 105 70 105 minecraft:air',

  // Continue running all steps even if one fails (collect full failure picture)
  stopOnFail: false,

  steps: [
    { chat: '/say _hello scenario starting' },
    { lookAt: 'south' },
    { give: 'minecraft:stone 1 0' },
    { verifyInventory: { item: 'stone', meta: 0, count: 1 } },
    { place: { pos: '~ ~ ~+1', block: 'minecraft:stone', meta: 0 } },
    { verifyBlock: { pos: '~ ~ ~+1', name: 'stone', meta: 0 } },
    { break: '~ ~ ~+1' },
    { verifyBlock: { pos: '~ ~ ~+1', name: 'air' } },
    { chat: '/say _hello scenario done' },
  ],
};
