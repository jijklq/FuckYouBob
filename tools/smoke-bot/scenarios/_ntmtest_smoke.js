'use strict';
/**
 * CP3/CP4: smoke-тест для ntm-testhelper команд.
 * Запускать после деплоя ntm-testhelper-1.0.0.jar в testserver/mods/ и рестарта.
 *
 * Этот сценарий использует только chat-step (бот шлёт команды через чат),
 * результат виден в server log. Bot-side parsing [NTMTEST] → это B.3b.
 */
module.exports = {
  name: '_ntmtest_smoke — ручная проверка CP3/CP4',
  arena: { x: 100, y: 65, z: 100 },
  setup: ['/gamemode 1 @s'],
  cleanup: '/fill 95 60 95 105 70 105 minecraft:air',
  stopOnFail: false,
  steps: [
    // Prepare: place stone and NTM block
    { chat: '/setblock 100 65 100 minecraft:stone 0' },
    { wait: 500 },

    // CP3: block query on vanilla block
    { chat: '/ntmtest block 100 65 100' },
    { wait: 500 },

    // CP3: item query (hand)
    { chat: '/ntmtest item hand' },
    { wait: 300 },

    // CP3: effects
    { chat: '/ntmtest effects' },
    { wait: 300 },

    // CP3: canspawn
    { chat: '/ntmtest canspawn minecraft:zombie 100 65 100' },
    { wait: 300 },

    // CP3: entities in radius 20
    { chat: '/ntmtest entities 20' },
    { wait: 300 },

    // CP4: replace with NTM block (deco_crt variant=2 facing=east → meta=11)
    { chat: '/setblock 100 65 100 hbm:deco_crt 11' },
    { wait: 500 },
    { chat: '/ntmtest block 100 65 100' },
    { wait: 500 },

    // CP3: cleanup command
    { chat: '/ntmtest cleanup 95 60 95 105 70 105' },
    { wait: 800 },

    { chat: '/say _ntmtest_smoke done — check server log for [NTMTEST] lines' },
  ],
};
