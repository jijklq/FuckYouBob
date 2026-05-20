'use strict';
/**
 * B.4: BlockDecoToaster — 3 variants × 4 facings = 12 placements.
 * Critical: AABB rotation N/S → AABB_NS, E/W → AABB_EW (Bob's logic).
 */

const FACINGS  = ['north', 'south', 'west', 'east'];
const AABB_NS  = [0.25, 0, 0.375, 0.75, 0.325, 0.625];
const AABB_EW  = [0.375, 0, 0.25, 0.625, 0.325, 0.75];
const steps    = [];

let zOffset = 1;
for (let variant = 0; variant < 3; variant++) {
  for (let facingIdx = 0; facingIdx < 4; facingIdx++) {
    const facing       = FACINGS[facingIdx];
    const meta         = (variant << 2) | facingIdx;
    const expectedAabb = (facing === 'north' || facing === 'south') ? AABB_NS : AABB_EW;
    const z            = 100 + zOffset;

    steps.push({ chat: `/setblock 100 65 ${z} hbm:deco_toaster ${meta}` });
    steps.push({ wait: 200 });
    steps.push({ verifyNtmBlock: {
        pos:       `~ ~ ~+${zOffset}`,
        block:     'hbm:deco_toaster',
        meta:      meta,
        props:     { facing: facing, variant: variant },
        aabb:      expectedAabb,
        teNbtNull: true,
    } });
    steps.push({ break: `~ ~ ~+${zOffset}` });

    zOffset++;
  }
}

module.exports = {
  name:       '4.4c.toaster — BlockDecoToaster 3 variants × 4 facings + AABB rotation N/S vs E/W',
  arena:      { x: 100, y: 65, z: 100 },
  setup:      ['/gamemode 1 @s'],
  cleanup:    '/ntmtest cleanup 95 64 95 105 70 120',
  stopOnFail: false,
  steps,
};
