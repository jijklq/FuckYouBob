'use strict';
// CP4: negative scenario — verifyBlock expects wrong block name → should produce [assertion] failure
module.exports = {
  name: '_hello_neg — assertion failure test',
  arena: { x: 100, y: 65, z: 100 },
  setup: ['/gamemode 1 @s'],
  cleanup: '/fill 95 64 95 105 70 105 minecraft:air',
  stopOnFail: false,
  steps: [
    { place: { pos: '~ ~ ~+1', block: 'minecraft:stone', meta: 0 } },
    // This should fail: stone is at ~+1, not diamond_block
    { verifyBlock: { pos: '~ ~ ~+1', name: 'diamond_block' } },
    { break: '~ ~ ~+1' },
  ],
};
