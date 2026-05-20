'use strict';
const { TimeoutError } = require('./await');

// 3 failure types:
//   assertion — verify-step mismatch → likely PRODUCT bug
//   timeout   — step exceeded budget or awaitState timed out → ENV or PRODUCT hang
//   runtime   — JS exception, network drop, kick → TEST or INFRA bug
function classify(err) {
  if (err instanceof TimeoutError)                              return 'timeout';
  if (err.name === 'AssertionError' || err.code === 'ASSERTION') return 'assertion';
  return 'runtime';
}

module.exports = { classify };
