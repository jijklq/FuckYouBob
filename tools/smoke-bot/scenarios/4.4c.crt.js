'use strict';
/**
 * B.4: BlockDecoCRT — 4 variants × 4 facings = 16 placements.
 * Validates: meta encoding, props decode, full-cube AABB.
 */

const FACINGS         = ['north', 'south', 'west', 'east'];
const FULL_CUBE_AABB  = [0, 0, 0, 1, 1, 1];
const steps           = [];

let zOffset = 1;
for (let variant = 0; variant < 4; variant++) {
  for (let facingIdx = 0; facingIdx < 4; facingIdx++) {
    const facing  = FACINGS[facingIdx];
    const meta    = (variant << 2) | facingIdx;
    const z       = 100 + zOffset;

    steps.push({ chat: `/setblock 100 65 ${z} hbm:deco_crt ${meta}` });
    steps.push({ wait: 200 });
    steps.push({ verifyNtmBlock: {
        pos:       `~ ~ ~+${zOffset}`,
        block:     'hbm:deco_crt',
        meta:      meta,
        props:     { facing: facing, variant: variant },
        aabb:      FULL_CUBE_AABB,
        teNbtNull: true,
    } });
    steps.push({ break: `~ ~ ~+${zOffset}` });

    zOffset++;
  }
}

module.exports = {
  name:       '4.4c.crt — BlockDecoCRT 4 variants × 4 facings = 16 placements',
  arena:      { x: 100, y: 65, z: 100 },
  setup:      ['/gamemode 1 @s'],
  cleanup:    '/ntmtest cleanup 95 64 95 105 70 120',
  stopOnFail: false,
  steps,
};
