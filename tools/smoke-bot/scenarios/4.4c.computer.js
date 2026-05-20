'use strict';
/**
 * B.4: BlockDecoModel (deco_computer) — 4 facings × unique AABB = 4 placements.
 * setBlockBoundsTo(0.125, 0, 0, 0.875, 0.875, 0.625)
 * Tests yaw-mapping math: each facing must produce a distinct AABB.
 */

const FACINGS = ['north', 'south', 'west', 'east'];

// Per-facing AABBs from BlockDecoModel.getBoundingBox() switch:
//   NORTH: (1-mxX, mnY, 1-mxZ, 1-mnX, mxY, 1-mnZ)
//   SOUTH: (mnX,   mnY, mnZ,   mxX,   mxY, mxZ  )
//   WEST:  (1-mxZ, mnY, mnX,   1-mnZ, mxY, mxX  )
//   EAST:  (mnZ,   mnY, 1-mxX, mxZ,   mxY, 1-mnX)
const AABBS = {
  north: [0.125, 0, 0.375, 0.875, 0.875, 1.0  ],
  south: [0.125, 0, 0,     0.875, 0.875, 0.625],
  west:  [0.375, 0, 0.125, 1.0,   0.875, 0.875],
  east:  [0,     0, 0.125, 0.625, 0.875, 0.875],
};

const steps   = [];
const variant = 0; // DecoComputerEnum has a single value

let zOffset = 1;
for (let facingIdx = 0; facingIdx < 4; facingIdx++) {
  const facing       = FACINGS[facingIdx];
  const meta         = (variant << 2) | facingIdx;
  const expectedAabb = AABBS[facing];
  const z            = 100 + zOffset;

  steps.push({ chat: `/setblock 100 65 ${z} hbm:deco_computer ${meta}` });
  steps.push({ wait: 200 });
  steps.push({ verifyNtmBlock: {
      pos:       `~ ~ ~+${zOffset}`,
      block:     'hbm:deco_computer',
      meta:      meta,
      props:     { facing: facing, variant: variant },
      aabb:      expectedAabb,
      teNbtNull: true,
  } });
  steps.push({ break: `~ ~ ~+${zOffset}` });

  zOffset++;
}

module.exports = {
  name:       '4.4c.computer — BlockDecoModel 4 facings × 4 unique AABBs (yaw-mapping math)',
  arena:      { x: 100, y: 65, z: 100 },
  setup:      ['/gamemode 1 @s'],
  cleanup:    '/ntmtest cleanup 95 64 95 105 70 120',
  stopOnFail: false,
  steps,
};
