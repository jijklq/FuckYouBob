'use strict';
// CP5: timeout failure test — tp to unloaded coords, per-step budget 3s
module.exports = {
  name: '_hello_timeout — timeout failure test',
  arena: { x: 100, y: 65, z: 100 },
  setup: ['/gamemode 1 @s'],
  cleanup: '/fill 95 64 95 105 70 105 minecraft:air',
  stopOnFail: false,
  steps: [
    { chat: '/say timeout test starting' },
    // wait:5000 but budget only 2000 → Promise.race fires TimeoutError
    { wait: 5000, _timeoutMs: 2000 },
    { chat: '/say this should still run (stopOnFail: false)' },
  ],
};
