'use strict';
const { sleep } = require('../utils/await');

module.exports = async (ctx, message) => {
  ctx.bot.chat(message);
  await sleep(100);
};
