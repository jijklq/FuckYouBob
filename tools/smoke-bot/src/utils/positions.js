'use strict';

function resolvePos(spec, anchor) {
  if (typeof spec === 'object' && spec !== null && 'x' in spec) {
    return { x: spec.x | 0, y: spec.y | 0, z: spec.z | 0 };
  }
  if (Array.isArray(spec)) {
    return { x: spec[0] | 0, y: spec[1] | 0, z: spec[2] | 0 };
  }
  if (typeof spec !== 'string') throw new Error(`resolvePos: bad spec ${JSON.stringify(spec)}`);

  const parts = spec.trim().split(/\s+/);
  if (parts.length !== 3) throw new Error(`resolvePos: expect 3 parts, got "${spec}"`);

  return {
    x: resolveCoord(parts[0], anchor?.x ?? 0),
    y: resolveCoord(parts[1], anchor?.y ?? 0),
    z: resolveCoord(parts[2], anchor?.z ?? 0),
  };
}

function resolveCoord(token, anchorValue) {
  if (token === '~') return anchorValue;
  if (token.startsWith('~')) {
    const offset = parseInt(token.slice(1), 10);
    if (isNaN(offset)) throw new Error(`bad offset: ${token}`);
    return anchorValue + offset;
  }
  const abs = parseInt(token, 10);
  if (isNaN(abs)) throw new Error(`bad coord: ${token}`);
  return abs;
}

module.exports = { resolvePos };
