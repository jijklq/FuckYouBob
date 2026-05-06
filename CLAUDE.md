# NTM 1.12.2 Port

## Что это
Порт форка HBM's Nuclear Tech (с космосом) с 1.7.10 на 1.12.2.
Оригинальный форк: https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT

## Окружение
- Java: JDK 1.8.0_202 (`C:\Program Files\Java\jdk1.8.0_202`)
- IDE: IntelliJ IDEA 2022.x, SDK = Java 8
- Forge: 1.12.2-14.23.5.2859
- ForgeGradle: 5.1.+
- Gradle wrapper: 7.6.4
- Mappings: snapshot 20171003-1.12
- Команда сборки: `gradlew build`
- Тестовый сервер: VPS с панелькой, деплой через .jar

## Правила кода (из CONTRIBUTING)
- `I18nUtil` вместо `I18n` — всегда, без исключений
- Не добавлять новые библиотеки без крайней необходимости
- Не дублировать util-функции — использовать существующие
- Никаких половинчатых фич и TODO-заглушек в PR
- Каждый PR = одна механика, полностью рабочая
- Тестировать на клиенте И на сервере перед PR

## Стратегия порта
Переносим механики по одной, снизу вверх:
1. Скелет проекта (регистрация, конфиг, прокси)
2. Базовые материалы (блоки/айтемы без логики)
3. TileEntity — машины по одной
4. Рецепты и крафт
5. Worldgen (руды, структуры)
6. Рендеринг (параллельно, не блокирует логику)
7. Космос (форковая часть) — в конце

## Известные паттерны миграции 1.7.10 → 1.12.2

### Регистрация
```java
// 1.7.10
GameRegistry.registerBlock(block, "name");
GameRegistry.registerItem(item, "name");

// 1.12.2 — в SubscribeEvent на RegistryEvent
@SubscribeEvent
public static void registerBlocks(RegistryEvent.Register<Block> e) {
    e.getRegistry().register(block.setRegistryName("hbm", "name"));
}
```

### TileEntity
```java
// 1.7.10
GameRegistry.registerTileEntity(TileFoo.class, "hbm:foo");

// 1.12.2
GameRegistry.registerTileEntity(TileFoo.class, new ResourceLocation("hbm", "foo"));
```

### Рецепты
```java
// 1.7.10
GameRegistry.addRecipe(result, "XX", "XX", 'X', Items.IRON_INGOT);

// 1.12.2
GameRegistry.addShapedRecipe(new ResourceLocation("hbm", "foo"), null, result, "XX", "XX", 'X', Items.IRON_INGOT);
// Или через JSON в assets/hbm/recipes/
```

### Рендеринг итемов
```java
// 1.7.10 — в ClientProxy
MinecraftForgeClient.registerItemRenderer(...)

// 1.12.2
ModelLoader.setCustomModelResourceLocation(item, 0,
    new ModelResourceLocation("hbm:item_name", "inventory"));
```

## Статус порта
- [x] Скелет проекта (build.gradle, основной класс, прокси) — BUILD SUCCESSFUL
- [ ] Регистрация (блоки, айтемы, TileEntity)
- [ ] Конфиг
- [ ] ...дополняется по мере работы

## Для новых сессий
Прочитай этот файл и статус порта — этого достаточно чтобы продолжить.
Конкретную задачу сессии формулируй так:
"Переносим [название механики] — вот исходник: [ссылка на файл в репо]"
