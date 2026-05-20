# NTM Smoke Bot

Headless mineflayer bot для автоматизированного smoke-тестирования NTM-port.

## Setup

```bash
cd tools/smoke-bot
npm install
```

## Run B.1 (hello-world)

Требуется запущенный Forge 1.12.2 server с NTM jar на `localhost:25565`. См. `C:\Users\rad\cc\testserver\run.bat`.

```bash
npm run hello
```

Expected: bot logs in, says "hello", disconnects. Exit 0.

## Environment

- `MC_HOST` (default: localhost)
- `MC_PORT` (default: 25565)
- `MC_USER` (default: smokebot)
