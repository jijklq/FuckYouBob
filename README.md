# FuckYouBob — HBM NTM port to 1.12.2

Порт [HBM's Nuclear Tech](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT) (форк [James](https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT) с космосом и планетами) с Minecraft 1.7.10 на 1.12.2.

**Не аффилировано с HBM, Bobcat и авторами оригинала.** Название — внутренний мем, ничего личного.

## ⚠️ Вайбкод-дисклеймер

Большая часть кода в этом репо сгенерирована Claude (Sonnet/Opus) под надзором живого человека. Цель — максимально близкий перенос механик 1.7.10 без рефакторинга и отсебятины.

Из CONTRIBUTING оригинала:

> No refactor PRs. Your refactors suck ass and usually something ends up breaking.

— это правило применимо и к порту, особенно к нейронке.

Претензии по поводу AI-генерации **принимаются, но не читаются**. Если нашёл реальный баг — issue с repro приветствуется.

## Сборка

```bash
./gradlew build
```

Артефакт — `build/libs/ntm-1.0.0.jar`. CI крутится на каждый пуш — см. [`.github/workflows/build.yml`](.github/workflows/build.yml).

### Окружение

- Java 8 (JDK 1.8.0_202 или совместимый Corretto/Temurin/Zulu)
- Forge 1.12.2-14.23.5.2859
- ForgeGradle 5.1.+
- Gradle 7.6.4 (wrapper)
- Mappings: `snapshot_20171003-1.12`

JEI-API jar лежит в [`libs/`](libs/) — soft dependency, для запуска с JEI положи полный JEI-jar в `run/mods/`.

## Статус

Ранний WIP, на прод-сервер ставить рано. Что сделано:

- Скелет проекта, creative tabs (9 шт.)
- Регистрация блоков (~226) и айтемов (1929) — пока заглушки `new Item()`/`new Block()`
- JEI-интеграция (soft dependency)
- Конфиг Системы 1 + 2 + 3 (Forge `Configuration` + JSON через Gson)

Впереди — TileEntity, энергосети, флюидные сети, рецепты, worldgen, рендер, оружие Sedna, космос.

## Ссылки

- Upstream HBM: <https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT>
- James space fork (исходник этого порта): <https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT>
- Этот порт: <https://github.com/jijklq/FuckYouBob>

## Лицензия

См. [`LICENSE`](LICENSE), а также `LICENSE-Paulscode IBXM Library.txt` и `LICENSE-Paulscode SoundSystem CodecIBXM.txt` для embedded-зависимостей.
