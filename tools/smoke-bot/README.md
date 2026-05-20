# NTM Smoke Bot

Headless mineflayer bot для автоматизированного smoke-тестирования NTM-port (Forge 1.12.2).

## Setup

```bash
cd tools/smoke-bot
npm install
```

Требуется: Node.js, запущенный Forge 1.12.2 testserver на `localhost:25565`, бот op'нут (`smokebot`, offline UUID).

## Быстрый старт

```bash
# Проверить что ничего не сломалось (сервер уже запущен)
./deploy.sh --no-build --no-restart smoke_current

# Полный pipeline: build → restart → регрессия
./deploy.sh smoke_current
```

## deploy.sh

```bash
./deploy.sh [options] <scenario> [<scenario> ...]

--no-build      skip gradlew build (reuse existing jar)
--no-restart    reuse running server
--also-helper   rebuild + deploy ntm-testhelper jar too
```

Примеры:

```bash
# Итерация сценария без rebuild/restart (~25s)
./deploy.sh --no-build --no-restart smoke_current

# Перезапустить с текущим jar, прогнать один сценарий
./deploy.sh --no-build 4.4c.crt

# Полный pipeline, все 4.4c сценарии (~2 мин)
./deploy.sh 4.4c.crt 4.4c.toaster 4.4c.computer

# Пересобрать оба мода + полная регрессия
./deploy.sh --also-helper smoke_current 4.4c.crt 4.4c.toaster 4.4c.computer
```

Timings: gradlew build ~37s, server boot ~17s, smoke_current ~25s, полный 4.4c suite ~50s.

## Сценарии

| Файл | Что тестирует | Steps |
|---|---|---|
| `smoke_current.js` | **Регрессия всех портированных классов** (3.x–4.4c, 18 классов) | 74 |
| `4.4c.crt.js` | BlockDecoCRT — 4 variants × 4 facings, meta encoding + AABB | 64 |
| `4.4c.toaster.js` | BlockDecoToaster — 3 variants × 4 facings, AABB_NS vs AABB_EW | 48 |
| `4.4c.computer.js` | BlockDecoModel — 4-way yaw AABB math | 16 |
| `_hello_ntm.js` | verifyNtmBlock smoke: deco_crt, stone, toaster | 9 |
| `_hello.js` | Dry-run всех 9 step-типов + resilience | 9 |
| `_ntmtest_smoke.js` | Ручная проверка 6 `/ntmtest` subcommands (вывод в консоль) | 19 |

**Правило:** после каждой фазы порта с новым Java-классом блока — добавить представителя в `smoke_current.js`.

## Step-типы

```js
{ chat: '/setblock 100 65 100 hbm:deco_crt 11' }
{ wait: 400 }
{ give: 'minecraft:stone 1 0' }
{ place: { pos: '~ ~ ~+1', block: 'minecraft:stone', meta: 0 } }
{ break: '~ ~ ~+1' }
{ lookAt: 'south' }
{ tp: '100 65 100' }
{ verifyBlock:     { pos: '~ ~ ~+1', name: 'stone', meta: 0 } }
{ verifyInventory: { item: 'stone', count: 1 } }
{ verifyNtmBlock:  { pos: '~ ~ ~+1', block: 'hbm:deco_crt', meta: 11,
                     props: { facing: 'east', variant: 2 },
                     aabb: [0, 0, 0, 1, 1, 1], teNbtNull: true } }
```

`verifyNtmBlock` использует `/ntmtest block` через серверный мод и делает 3 retry × 400ms.

## /ntmtest subcommands (серверный мод)

Мод `ntm-testhelper-1.0.0.jar` должен лежать в `testserver/mods/`.

```
/ntmtest block <x> <y> <z>          → name, meta, props, AABB, hardness, opaque, te_nbt
/ntmtest item hand                   → id, count, meta, damage, nbt
/ntmtest effects                     → hp, food, air, xp, активные эффекты
/ntmtest canspawn <entity> <x> <y> <z>
/ntmtest entities <radius>           → count + полный NBT каждого entity
/ntmtest cleanup <x1> <y1> <z1> <x2> <y2> <z2>  → fill air + kill entities
```

Ответы приходят в чат бота в формате `[NTMTEST] key=value`, парсятся автоматически в `verifyNtmBlock`.

## Структура

```
src/
  bot.js                  — B.1 hello-world (npm run hello)
  scenario-runner.js      — движок сценариев
  steps/
    chat.js / give.js / place.js / break.js / lookAt.js / tp.js / wait.js
    verifyBlock.js        — проверка через mineflayer blockAt()
    verifyInventory.js
    verifyNtmBlock.js     — проверка через /ntmtest block
  utils/
    connect.js            — Forge handshake, DEFAULT_FORGE_MODS
    await.js              — awaitState, retry, TimeoutError, sleep
    classify.js           — [assertion] / [timeout] / [runtime]
    ntmtest.js            — awaitNtmtestResponse (парсинг [NTMTEST] chat)
    positions.js          — resolvePos ('~ ~ ~+1' → {x,y,z})
scenarios/
  smoke_current.js        — ← главный регрессионный сценарий
  4.4c.{crt,toaster,computer}.js
  _hello.js / _hello_ntm.js / _ntmtest_smoke.js
deploy.sh                 — build + restart + run pipeline

../testhelper-mod/        — Forge мод /ntmtest (отдельный gradle build)
```

## Environment

- `MC_HOST` (default: localhost)
- `MC_PORT` (default: 25565)
- `MC_USER` (default: smokebot)
