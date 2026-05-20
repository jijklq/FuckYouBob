'use strict';
const { sleep } = require('../utils/await');

// Escape hatch — unconditional sleep. Prefer awaitState in steps. Use sparingly.
// Usage: { wait: 500 }
module.exports = async (ctx, ms) => sleep(ms);
