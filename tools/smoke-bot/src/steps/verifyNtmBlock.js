'use strict';

const { resolvePos }           = require('../utils/positions');
const { awaitNtmtestResponse } = require('../utils/ntmtest');
const { retry }                = require('../utils/await');

class AssertionError extends Error {
  constructor(msg) {
    super(msg);
    this.name = 'AssertionError';
    this.code = 'ASSERTION';
  }
}

const AABB_TOLERANCE = 0.001;

/**
 * Verify a block via /ntmtest block <x> <y> <z>.
 *
 * Usage:
 *   { verifyNtmBlock: {
 *       pos:       '~ ~ ~+1',            // required
 *       block:     'hbm:deco_crt',       // optional — check registry name
 *       meta:      11,                   // optional — check raw meta int
 *       props:     { facing: 'east',     // optional — check IBlockState props
 *                    variant: 2 },
 *       aabb:      [0, 0, 0, 1, 1, 1],   // optional — 6 floats [minX,minY,minZ,maxX,maxY,maxZ]
 *       teNbtNull: true,                 // optional — true=expect null, false=expect present
 *   } }
 */
module.exports = async (ctx, expect) => {
  const pos = resolvePos(expect.pos, ctx.anchor);
  const coordStr = `${pos.x} ${pos.y} ${pos.z}`;

  await retry(async () => {
    const r = await awaitNtmtestResponse(ctx.bot, `block ${coordStr}`);

    // --- block name ---
    if (expect.block !== undefined) {
      const actual = r.get('block');
      if (actual !== expect.block) {
        throw new AssertionError(
          `verifyNtmBlock block: expected "${expect.block}", got "${actual}"`
        );
      }
    }

    // --- meta ---
    if (expect.meta !== undefined) {
      const actual = parseInt(r.get('meta'), 10);
      if (actual !== expect.meta) {
        throw new AssertionError(
          `verifyNtmBlock meta: expected ${expect.meta}, got ${actual}`
        );
      }
    }

    // --- props ---
    if (expect.props !== undefined) {
      // Server format: "{key1=val1,key2=val2}"
      const raw    = r.get('props') || '{}';
      const inner  = raw.replace(/^\{|\}$/g, '');
      const actual = inner === '' ? [] : inner.split(',').sort();

      const expected = Object.entries(expect.props)
        .map(([k, v]) => `${k}=${v}`)
        .sort();

      if (JSON.stringify(actual) !== JSON.stringify(expected)) {
        throw new AssertionError(
          `verifyNtmBlock props: expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`
        );
      }
    }

    // --- aabb ---
    if (expect.aabb !== undefined) {
      const rawAabb = r.get('aabb') || '';
      const actual  = rawAabb.split(',').map(parseFloat);

      if (actual.length !== 6 || actual.some(isNaN)) {
        throw new AssertionError(
          `verifyNtmBlock aabb: malformed value "${rawAabb}"`
        );
      }

      for (let i = 0; i < 6; i++) {
        if (Math.abs(actual[i] - expect.aabb[i]) > AABB_TOLERANCE) {
          throw new AssertionError(
            `verifyNtmBlock aabb[${i}]: expected ${expect.aabb[i]}, got ${actual[i]}`
          );
        }
      }
    }

    // --- te_nbt ---
    if (expect.teNbtNull === true && r.get('te_nbt') !== 'null') {
      throw new AssertionError(
        `verifyNtmBlock: expected te_nbt=null, got "${r.get('te_nbt')}"`
      );
    }
    if (expect.teNbtNull === false && r.get('te_nbt') === 'null') {
      throw new AssertionError(
        `verifyNtmBlock: expected te_nbt present, got null`
      );
    }

  }, { attempts: 3, initialDelay: 400, label: 'verifyNtmBlock' });
};
