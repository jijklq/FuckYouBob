'use strict';
/**
 * smoke_current.js — регрессионный smoke всех портированных классов блоков.
 * Один представитель от каждого класса. Обновляется после каждой фазы порта.
 *
 * Запуск: ./deploy.sh --no-build --no-restart smoke_current
 *
 * Текущее покрытие: этапы 3.x–4.4f (23 классов)
 *
 * ПРАВИЛО: после каждого нового Java-класса блока — добавить сюда 1 блок.
 * Делать в той же сессии что и сам порт.
 */

const ARENA_X = 100;
const ARENA_Y = 65;
const ARENA_Z = 100;

// Helper: generate steps for one block placement
// z is absolute Z coordinate
function blockEntry(z, registryName, meta, opts = {}) {
  const off = z - ARENA_Z;
  const steps = [];

  // Optional: prepare block below (for plants needing soil)
  if (opts.soilBelow) {
    steps.push({ chat: `/setblock ${ARENA_X} ${ARENA_Y - 1} ${z} minecraft:dirt 0` });
    steps.push({ wait: 100 });
  }
  // Optional: place water above (for aquatic blocks)
  if (opts.waterAbove) {
    steps.push({ chat: `/setblock ${ARENA_X} ${ARENA_Y + 1} ${z} minecraft:water 0` });
    steps.push({ wait: 100 });
  }

  steps.push({ chat: `/setblock ${ARENA_X} ${ARENA_Y} ${z} ${registryName} ${meta}` });
  steps.push({ wait: 200 });

  const verify = { pos: `~ ~ ~+${off}`, block: registryName, meta };
  if (opts.props) verify.props = opts.props;
  if (opts.aabb)  verify.aabb  = opts.aabb;
  steps.push({ verifyNtmBlock: verify });

  steps.push({ break: `~ ~ ~+${off}` });
  return steps;
}

const steps = [].concat(
  // ─── Ore family ──────────────────────────────────────────────
  // BlockOreOutgas: ore with outgas behavior
  blockEntry(101, 'hbm:ore_uranium',    0),
  // BlockOre: plain ore
  blockEntry(102, 'hbm:ore_copper',     0),
  // BlockCluster: crystal cluster drops
  blockEntry(103, 'hbm:cluster_iron',   0),

  // ─── Falling blocks ──────────────────────────────────────────
  // BlockFallingTint: colored falling block (laythe_silt)
  blockEntry(104, 'hbm:laythe_silt',    0),

  // ─── Rad-hazard family ───────────────────────────────────────
  // BlockHazard: radiation + potion effects on proximity
  blockEntry(105, 'hbm:block_uranium',  0),
  // BlockHotHazard: + cloud particles in rain
  blockEntry(106, 'hbm:block_polonium', 0),
  // BlockFallout: sand-like fallout block
  blockEntry(107, 'hbm:fallout',        0),
  // BlockHazardFalling: falling fallout
  blockEntry(108, 'hbm:block_fallout',  0),
  // BlockSellafield: sellafield ore/block
  blockEntry(109, 'hbm:sellafield',     0),

  // ─── Waste / taint ───────────────────────────────────────────
  // WasteEarth: 4 variants (waste, mycelium, frozen, burning)
  blockEntry(110, 'hbm:waste_earth',    0),
  // BlockTaint: spreading taint block
  blockEntry(111, 'hbm:taint',          0),

  // ─── Leaves ──────────────────────────────────────────────────
  // BlockRubberLeaves: leaves with CHECK_DECAY + DECAYABLE props
  // meta=0 → decayable=true, check_decay=false
  blockEntry(112, 'hbm:rubber_leaves',  0, {
    props: { decayable: 'true', check_decay: 'false' },
  }),

  // ─── Plants (need soil below) ────────────────────────────────
  // BlockNTMFlower (BlockEnumMulti, 8 variants): needs dirt/grass below
  blockEntry(113, 'hbm:plant_flower',   0, { soilBelow: true }),

  // ─── Deco blocks ─────────────────────────────────────────────
  // BlockDecoCT: 9 metal types, plain solid block
  blockEntry(114, 'hbm:deco_titanium',  0),
  // BlockPipe: pipe with 4 rotation types
  blockEntry(115, 'hbm:deco_pipe',      0),
  // BlockDecoCRT: FACING × VARIANT, full-cube AABB
  blockEntry(116, 'hbm:deco_crt',       11, {
    props: { facing: 'east', variant: 2 },
    aabb:  [0, 0, 0, 1, 1, 1],
  }),
  // BlockDecoToaster: custom AABB_NS (facing=north)
  blockEntry(117, 'hbm:deco_toaster',   0, {
    props: { facing: 'north', variant: 0 },
    aabb:  [0.25, 0, 0.375, 0.75, 0.325, 0.625],
  }),
  // BlockDecoModel (deco_computer): 4-way yaw AABB, facing=north
  blockEntry(118, 'hbm:deco_computer',  0, {
    props: { facing: 'north', variant: 0 },
    aabb:  [0.125, 0, 0.375, 0.875, 0.875, 1.0],
  }),

  // ─── Lamp family ─────────────────────────────────────────────
  // ReinforcedLamp: redstone on/off pair, cube_all, off variant
  blockEntry(119, 'hbm:reinforced_lamp_off', 0),
  // TritiumLamp: redstone on/off + ISpotlight, off variant (green)
  blockEntry(120, 'hbm:lamp_tritium_green_off', 0),

  // ─── Surface/structure family (4.4f) ────────────────────────
  // BlockSpeedy: speed multiplier on player walk (asphalt, speed=1.5)
  blockEntry(121, 'hbm:asphalt', 0),
  // BlockSandbags: connected-AABB (same-block-only simplified)
  blockEntry(122, 'hbm:sandbags', 0),
  // BlockBarrier: facing meta PropertyInteger 0..5; place with meta=2 (NEG_Z/NORTH wall)
  blockEntry(123, 'hbm:wood_barrier', 2),
);

module.exports = {
  name:       'smoke_current — регрессия портированных классов блоков (3.x–4.4f)',
  arena:      { x: ARENA_X, y: ARENA_Y, z: ARENA_Z },
  setup:      [
    '/gamemode 1 @s',
    `/fill ${ARENA_X - 5} ${ARENA_Y - 1} ${ARENA_Z - 5} ${ARENA_X + 5} ${ARENA_Y - 1} ${ARENA_Z + 25} minecraft:stone`,
  ],
  cleanup:    '/ntmtest cleanup 94 63 99 106 72 124',
  stopOnFail: false,
  steps,
};
