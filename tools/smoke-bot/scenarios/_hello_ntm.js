'use strict';
/**
 * B.3b smoke test: verifyNtmBlock against real hbm:deco_crt and vanilla stone.
 * Pre-condition: test-server running with ntm-testhelper-1.0.0.jar loaded.
 */
module.exports = {
  name: '_hello_ntm — verifyNtmBlock smoke против hbm:deco_crt',
  arena: { x: 100, y: 65, z: 100 },
  setup: ['/gamemode 1 @s'],
  cleanup: '/ntmtest cleanup 95 64 95 105 70 105',
  stopOnFail: false,
  steps: [
    // --- CP2: NTM block (deco_crt meta=11 = facing=east, variant=2) ---
    { chat: '/setblock 100 65 101 hbm:deco_crt 11' },
    { wait: 400 },
    { verifyNtmBlock: {
        pos:       '~ ~ ~+1',
        block:     'hbm:deco_crt',
        meta:      11,
        props:     { facing: 'east', variant: 2 },
        aabb:      [0, 0, 0, 1, 1, 1],
        teNbtNull: true,
    } },

    // --- Vanilla block sanity ---
    { chat: '/setblock 100 65 102 minecraft:stone 0' },
    { wait: 200 },
    { verifyNtmBlock: {
        pos:   '~ ~ ~+2',
        block: 'minecraft:stone',
        meta:  0,
        aabb:  [0, 0, 0, 1, 1, 1],
    } },

    // --- CP5 (bonus): BlockDecoToaster AABB ---
    // hbm:deco_toaster meta=0 → facing=north, variant=0 → AABB_NS
    { chat: '/setblock 100 65 103 hbm:deco_toaster 0' },
    { wait: 300 },
    { verifyNtmBlock: {
        pos:   '~ ~ ~+3',
        block: 'hbm:deco_toaster',
        aabb:  [0.25, 0, 0.375, 0.75, 0.325, 0.625],
    } },
  ],
};
