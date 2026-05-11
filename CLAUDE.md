# NTM 1.12.2 Port

## Что это
Порт форка HBM's Nuclear Tech (с космосом) с 1.7.10 на 1.12.2.
- Оригинальный форк: https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT
- Наше репо: https://github.com/jijklq/FuckYouBob
- Исходник 1.7.10: `C:\Users\rad\cc\Hbm-s-Nuclear-Tech-GIT-space-travel-twopointfive\`

## Окружение
- Java: JDK 1.8.0_202 (`C:\Program Files\Java\jdk1.8.0_202`) — Java 8 синтаксис, без `var`/`List.of`/text blocks
- IDE: IntelliJ IDEA, SDK = Java 8
- Forge: 1.12.2-14.23.5.2859, ForgeGradle: 5.1.+, Gradle wrapper: 7.6.4
- Mappings: snapshot 20171003-1.12 — метод `setUnlocalizedName`, **не** `setTranslationKey`
- Сборка: `gradlew build` | CI: GitHub Actions на каждый пуш
- Дев-сервер: VPS, MCSManager, Forge 1.12.2, Java 8 (zulu_8), порт 25560

## Принципы порта

Цель — максимально близкий перенос механик 1.7.10 в 1.12.2 без рефакторинга. Из CONTRIBUTING оригинала: «No refactor PRs. Your refactors suck ass and usually something ends up breaking.» Это правило применимо и к нам.

- **Перед кодом — читать оригинал целиком**, не первые 50 строк. Выписать сигнатуры, типы полей, константы. Не угадывать имена классов HBM — они часто неочевидны (`PowerNetMK2`, `IFluidStandardTransceiver`, `WorldChunkManagerCelestial`).
- **Не менять типы**: `long` для энергии остаётся `long` (большие реакторы переполняют `int`), `null` там где был `null`, конкретный `HashMap` не превращается в абстрактный `Map`. Исключение — `ItemStack`, см. «Семантические изменения».
- **Не рефакторить за пределами задачи**: переименования, for→stream, inner class→lambda, объединение методов — это не миграция.
- **Не хардкодить то, что было конфигом**: число или строка в коде → проверить `com/hbm/config/*`. Конфиг переносится одновременно с механикой, не «потом» (см. «Конфиг»).
- **Сохранять сигнатуры публичного API**: `api/hbm/*` — публичный, от него зависят аддоны.
- **Не оставлять TODO и половинчатых реализаций**. Если задача не выполнима полностью — остановиться и обсудить, а не писать заглушку.
- **Большие задачи — план до кода**. Если задача — это >20 однотипных операций, или >5 файлов, или >200 строк — сначала прислать план в чат и дождаться подтверждения. Подробнее — «Декомпозиция и контроль больших задач».
- **Перед «готово»**: `BUILD SUCCESSFUL` + smoke-тест на VPS, для критичных механик — стресс-тесты из соответствующего раздела.

## Декомпозиция и контроль больших задач

Главное правило: **большие задачи разбиваются на подзадачи с явными чекпойнтами**, а не выполняются «одной простыни». Контроль важнее скорости — без чекпойнтов сессии заканчиваются после 100k+ токенов на повторяющемся коде, который скрипт сделал бы за минуту.

### Признаки «большой» задачи (нужна декомпозиция)
- > 20 однотипных операций (регистраций, JSON-моделей, переносов рецептов одного типа)
- Затрагивает > 5 файлов или > 200 строк нового/изменённого кода
- Содержит несколько подсистем механики (блок + TE + GUI + рендер + рецепты)
- Объём непонятен заранее — оценить **до** начала работы

### Перед началом большой задачи
1. Оценить объём (число файлов, строк, items)
2. **Прислать план в чат**: список этапов и чекпойнтов между ними
3. Дождаться подтверждения плана от пользователя
4. Выполнять по этапам, между этапами — `BUILD SUCCESSFUL` и явное «дальше» от пользователя

### Скрипт вместо ручного перебора
Если задача — это N однотипных операций (N > ~20), **сначала писать скрипт-генератор**, потом разбирать особые случаи вручную.

Подходит для: регистрация блоков/айтемов чанк-ами, генерация JSON-моделей и blockstates, перенос рецептов одного типа, массовое переименование, замена импортов.

Порядок:
1. Скрипт (Python, ~50–150 строк) генерирует код/JSON
2. Запустить, получить вывод, вставить в проект
3. Найти и исправить особые случаи вручную (~10–30 штук)

Цена: ~5к токенов на скрипт против 50–110к на ручной перебор.

### Чекпойнты внутри одной задачи
Даже если задача не «N штук», но крупная:
- После каждого логически целого блока (~150 строк нового кода) — `gradlew build`, краткий статус в чат, ждать «дальше»
- Не писать 500+ строк новых классов «одним выстрелом» — расти инкрементально, проверяя сборку
- Если по ходу задача неожиданно стала большой — **остановиться и переразбить**, а не ломиться дальше

---

## Стиль (помимо CONTRIBUTING)

- `I18nUtil` вместо `I18n`
- Никаких новых библиотек
- Не дублировать util-функции
- Каждый PR = одна механика, полностью рабочая
- Тестировать на клиенте И на сервере (CONTRIBUTING требует обоих)
- Комментарии — только если они есть в оригинале и переносятся как есть

## Что переносить осторожно (HBM-специфика, не очевидно из кода)

- HBM-сети энергии и флюидов — кастомные, **не** заменять на Forge `IEnergyStorage` / `IFluidHandler`. Подробно — в разделах «Энергосети» и «Флюидные сети».
- Энергия в HBM — `long`, не `int`. Сохранять тип во всех буферах и счётчиках.
- Трубы в HBM **типизированы** по флюиду — не превращать в universal pipes.
- Конфиг разбит на ~17 файлов; имена опций сохранять, чтобы пользовательские конфиги переносились копи-пастой.
- Forge capabilities (`IItemHandler`, `IFluidHandler`, `IEnergyStorage`) — отдельная задача после порта, не при переносе механик. Если в оригинале `IInventory` — оставить `IInventory`.
- Часть блоков и айтемов — заглушки оригинала. Не удалять самостоятельно (см. «Легаси»).

## Когда остановиться и спросить

Если: не нашёл оригинальный файл механики; нашёл `// TODO`/`// FIXME`/обфусцированный код; API радикально изменился без очевидной замены; задача требует менять публичный `api/hbm/*`; нужно удалить много кода (>50 строк); сборка падает после двух попыток исправить — остановиться, описать что видно, спросить.

---

## Стратегия порта (общая последовательность)

1. ✓ Скелет проекта
2. (в процессе) Базовые блоки/айтемы без логики
3. **Конфиг** — параллельно с механиками
4. Простые TileEntity (печи, генераторы)
5. **Энергосети и флюидные сети** — критично
6. Сложные TE и подсистемы (RBMK/нейтронный поток, центрифуги, Foundry/плавильня, оружие/анимация — см. «Сложные механики»)
7. Атмосфера и герметичность (требует флюидной сети) — см. «Сложные механики»
8. Рецепты
9. Worldgen
10. Рендеринг
11. **Космос, гравитация и планеты** — последним

Большие задачи разбивать заранее по плану — см. «Декомпозиция и контроль больших задач».

---

## Миграция 1.7.10 → 1.12.2 (справочник)

### Регистрация блоков
```java
// initBlocks():
ore_uranium = new Block(Material.ROCK)
    .setUnlocalizedName("ore_uranium")
    .setRegistryName("hbm", "ore_uranium")
    .setCreativeTab(MainRegistry.blockTab)
    .setHardness(5.0F).setResistance(10.0F);

// SubscribeEvent на RegistryEvent.Register<Block>:
event.getRegistry().register(ore_uranium);

// SubscribeEvent на RegistryEvent.Register<Item>:
event.getRegistry().register(new ItemBlock(ore_uranium).setRegistryName(ore_uranium.getRegistryName()));
```

### TileEntity (регистрация)
```java
GameRegistry.registerTileEntity(TileFoo.class, new ResourceLocation("hbm", "foo"));
```

### Замены (rename mappings)
| 1.7.10 | 1.12.2 |
|---|---|
| `Material.rock`/`iron`/`ground` | `Material.ROCK`/`IRON`/`GROUND` |
| `cpw.mods.fml.*` | `net.minecraftforge.fml.*` |
| `setBlockName` | `setUnlocalizedName` |
| `setBlockTextureName(..)` | удалить (текстура из blockstate JSON, см. «Asset-файлы») |
| `getTabIconItem(): Item` | `getTabIconItem(): ItemStack` |
| `GameRegistry.registerBlock` | `event.getRegistry().register` в `RegistryEvent.Register<Block>` |
| `GameRegistry.registerItem` | то же для `Register<Item>` |
| `GameRegistry.registerTileEntity(cls, "id")` | то же, но `new ResourceLocation("hbm", "id")` |
| `xCoord`, `yCoord`, `zCoord` (TE) | `pos.getX()`, `pos.getY()`, `pos.getZ()` или `this.pos` |
| `worldObj` (TE) | `world` |
| `null` для пустого `ItemStack` | `ItemStack.EMPTY` + `stack.isEmpty()` (см. ниже) |
| `ItemStack[]` / `List<ItemStack>` инвентарь | `NonNullList<ItemStack>` |
| `updateEntity()` | `update()` + `class TileFoo implements ITickable` |
| `writeToNBT(nbt): void` | `writeToNBT(nbt): NBTTagCompound` (возвращать `nbt`) |
| `getDescriptionPacket()` | `getUpdatePacket()` + `getUpdateTag()` + `onDataPacket()` |
| `world.setBlock(x, y, z, block)` | `world.setBlockState(pos, block.getDefaultState())` |
| `world.getBlock(x, y, z)` | `world.getBlockState(pos).getBlock()` |
| `world.getBlockMetadata(x, y, z)` | `state.getValue(SOME_PROPERTY)` через `IProperty` |
| `getRenderBlockPass(): int` | `getBlockLayer(): BlockRenderLayer` |
| `getSubItems(Item, CreativeTabs, List)` | `getSubItems(CreativeTabs, NonNullList<ItemStack>)` |
| `world.playSoundEffect(x, y, z, name, v, p)` | `world.playSound(player, pos, SoundEvent, SoundCategory, v, p)` |

### Семантические изменения (не просто rename)

#### ItemStack.EMPTY
В 1.7.10 пустой `ItemStack` — это `null`. В 1.12.2 — синглтон `ItemStack.EMPTY` (`null != EMPTY`). Старые null-чеки тихо проходят и игра ведёт себя странно. Каждое `stack == null` / `stack != null` нужно заменить на `stack.isEmpty()` / `!stack.isEmpty()`, и присваивания `null` — на `ItemStack.EMPTY`.

#### NonNullList для инвентарей
Стандарт 1.12.2:
```java
private final NonNullList<ItemStack> inv = NonNullList.<ItemStack>withSize(SIZE, ItemStack.EMPTY);
```
Если в оригинале `ItemStack[]` — приемлемо оставить массивом с `ItemStack.EMPTY` во всех слотах, но не смешивать оба подхода в одном TE.

#### Metadata → IBlockState
Состояние блока (направление, on/off, level) переезжает из metadata 0–15 в `IBlockState` + `IProperty<T>`:
```java
public class BlockMachine extends Block {
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockMachine() {
        super(Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ACTIVE, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTIVE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ACTIVE) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(ACTIVE, meta == 1);
    }
}
```
Доступные `IProperty`: `PropertyBool`, `PropertyInteger`, `PropertyDirection`, `PropertyEnum<E>`.

Стратегия для блоков с metadata в 1.7.10:
- Metadata = разные типы (руды, материалы) → отдельные блоки (как сделано для руд в этом порте)
- Metadata = состояние одного блока (направление, on/off) → `IProperty`

### Asset-файлы (1.12.2)

В 1.7.10 текстура задавалась через `setBlockTextureName(..)`. В 1.12.2 — JSON:
```
src/main/resources/assets/hbm/
├── blockstates/ore_uranium.json     # описание состояний блока
├── models/
│   ├── block/ore_uranium.json       # 3D модель блока
│   └── item/ore_uranium.json        # модель в инвентаре
├── textures/blocks/ore_uranium.png  # текстура
└── lang/en_us.lang                  # tile.ore_uranium.name=Uranium Ore
```

Минимальный JSON для простого кубического блока:
```json
// blockstates/ore_uranium.json
{ "variants": { "normal": { "model": "hbm:ore_uranium" } } }
```
```json
// models/block/ore_uranium.json
{ "parent": "block/cube_all", "textures": { "all": "hbm:blocks/ore_uranium" } }
```
```json
// models/item/ore_uranium.json
{ "parent": "hbm:block/ore_uranium" }
```

Регистрация модели айтема — обязательно, иначе айтем — фиолетово-чёрная шахматка в инвентаре:
```java
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "hbm", value = Side.CLIENT)
public class ClientRegistry {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        Item ib = Item.getItemFromBlock(ModBlocks.ore_uranium);
        ModelLoader.setCustomModelResourceLocation(
            ib, 0, new ModelResourceLocation(ib.getRegistryName(), "inventory"));
    }
}
```

Для блоков с `IProperty` — в blockstate JSON variants по одному на комбинацию свойств, либо Forge blockstate format (более гибкий, поддерживает submodels — детали в Forge доках).

### Менее частые изменения (когда понадобится)
- **Звуки**: регистрация через `RegistryEvent.Register<SoundEvent>` + `assets/<modid>/sounds.json`. Воспроизведение — `world.playSound(player, pos, SoundEvent, SoundCategory, vol, pitch)`.
- **Vanilla-рецепты** (shaped/shapeless): JSON в `assets/<modid>/recipes/*.json`. `GameRegistry.addRecipe(..)` устарел. Печные рецепты — `GameRegistry.addSmelting(..)` ещё работает. Кастомные машинные рецепты HBM остаются кодом.
- **Entity registration**: API в Forge 1.12.2 менялся (`EntityEntryBuilder` / `RegistryEvent.Register<EntityEntry>`). Точный паттерн проверить в Forge 1.12.2 docs перед портом entity.
- **Particles**: стандартные через `EnumParticleTypes` и `world.spawnParticle(..)`. Кастомные — `IParticleFactory` (точный API проверить).
- **Команды**: `CommandBase`/`ICommand` — небольшие изменения; регистрация в `FMLServerStartingEvent` через `event.registerServerCommand(..)`.
- **Damage sources**: `new DamageSource("name")` + setters — без больших изменений.

### Псевдомногоблоки
Машины из нескольких блоков через dummy. На шаге 2 (блоки без логики): dummy = `new Block(Material.IRON)`. Логика структуры — на шаге TileEntity.

---

## Боилерплейт TileEntity

Шаблон со всеми обязательными методами 1.12.2 (включая server↔client sync — про него легко забыть, и баг проявляется только при загрузке чанка клиентом):

```java
public class TileFoo extends TileEntity implements ITickable {

    private long energy = 0L;
    private final NonNullList<ItemStack> inv = NonNullList.<ItemStack>withSize(SLOTS, ItemStack.EMPTY);

    @Override
    public void update() {
        try { doTick(); }
        catch (Exception e) {
            MainRegistry.logger.error("TE tick failed at " + pos, e);
            // не крашим сервер — пропускаем тик
        }
    }

    private void doTick() {
        // вся логика тика — здесь
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.energy = nbt.hasKey("energy") ? nbt.getLong("energy") : 0L;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("energy", this.energy);
        return nbt; // сигнатура поменялась с void — обязательно возвращать
    }

    // Sync server → client при первой загрузке chunk-а
    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    // Sync server → client на последующих обновлениях (через markDirty + notifyBlockUpdate)
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }
}
```

После любого изменения сохраняемых данных — `this.markDirty()`. Чтобы клиент сразу увидел изменение блока — `world.notifyBlockUpdate(pos, state, state, 3)` (флаг 3 = обновить соседей + отправить клиенту).

`getUpdatePacket()` / `getUpdateTag()` нужны только TE, чьё состояние видно на клиенте (рендер машины меняется, GUI показывает буфер). Чисто серверным TE — можно не реализовывать.

### Бюджеты тяжёлых операций
- Взрыв: ≤ N блоков за тик, остаток в очередь на следующий тик (`server.addScheduledTask`)
- Графы (энерго/флюид): max depth обхода = 1000
- Радиация: ≤ 1 чанк за тик
- Тяжёлые расчёты — async через `CompletableFuture`, результат на главный поток через `server.addScheduledTask()`

### Запрещено в TE
- Писать в NBT данные неограниченного размера (List без maxSize)
- Хранить ссылку на `World` или другой `TileEntity` после unload чанка
- Бесконечный цикл/рекурсия в `tick()`

---

## КРИТИЧНО: Энергосети (провода)

Кастомная HBM-система (HE), не RF/FE/Tesla. Самая важная механика мода. Ошибки → потеря энергии, взаимоблокировки, чанкбаны.

### Где это в оригинале
- `api/hbm/energymk2/` — публичный API:
  - `IEnergyConductorMK2`, `IEnergyConnectorBlock`, `IEnergyConnectorMK2`
  - `IEnergyHandlerMK2`, `IEnergyProviderMK2`, `IEnergyReceiverMK2`
  - `PowerNetMK2`
- `com/hbm/uninos/networkproviders/PowerNetProvider.java` — провайдер сети
- В моде есть и MK1 (старая) и MK2 — переносить MK2; MK1 трогать только для legacy-машин
- `cofh/api/energy/*` — старый RF-мост, не подменять им HBM-сеть

### Правила порта
1. Сигнатуры интерфейсов `api/hbm/energymk2/*` не менять — от них зависят аддоны
2. Все буферы и счётчики энергии — `long`. Не `int`. Не `float`.
3. Не подменять Forge `IEnergyStorage`. Если нужен мост — отдельный wrapper, после полного порта
4. Граф пересчитывается **только при изменении топологии** (поставлен/сломан провод/машина), не каждый игровой тик
5. Лимит глубины обхода — 1000 узлов
6. При unload чанка сеть расщепляется корректно. Не падает NPE на `world.getTileEntity` для unloaded позиций
7. Слияние/разделение сетей: провод между двумя сетями — одна; разрушенный посередине — две
8. Уровни напряжения (LV/MV/HV): throughput-лимит провода, превышение → плавится. Логику плавления сохранить

### Definition of done
- `gradlew build` → BUILD SUCCESSFUL
- Юнит-тесты на распределение энергии в простой сети — проходят
- 4 стресс-теста в игре:
  1. 1000 проводов в линию + генератор + потребитель → нет лагов, баланс сходится
  2. Кольцо проводов (loop) → не зависает
  3. Загрузка/разгрузка чанков с активной сетью → нет NPE, энергия не теряется и не дублируется
  4. HV-генератор на LV-провод → провод плавится, не передаёт дальше
- Релевантные опции конфига перенесены

---

## КРИТИЧНО: Флюидные сети (трубы)

Кастомная HBM-система. Не Forge `IFluidHandler` напрямую.

### Где это в оригинале
- `api/hbm/fluid/`:
  - `IFluidConnector`, `IFluidConnectorBlock`
  - `IFluidStandardReceiver`, `IFluidStandardSender`, `IFluidStandardTransceiver`
- Сетевой провайдер — рядом с `PowerNetProvider` в `com/hbm/uninos/networkproviders/`
- Регистрация флюидов — `com/hbm/inventory/fluid/*` (искать `Fluids` и `ModForgeFluids`)

### Правила порта
1. Трубы типизированы по типу флюида — не превращать в universal
2. Жидкости / газы / горячий пар — разные субсистемы (гравитация, давление). Сохранить разделение
3. Объёмы — `long` миллибаккеров для совместимости с большими резервуарами
4. Кастомные флюиды HBM регистрируются через `FluidRegistry` — порядок важен для NBT-сейвов
5. Pressure-rated трубы: превышение давления → разрыв (потеря). Логика разрыва — часть геймплея
6. Утечка: жидкость оставляет блок в мире, газ — ничего
7. Forge FluidStack / Fluid registry изменился между 1.7.10 и 1.12.2 (атрибуты, иконки, переводы). Кастомные флюиды переносить по одному, не пакетом

### Definition of done
- BUILD SUCCESSFUL
- Юнит-тесты на flow в простой сети — проходят
- 4 стресс-теста в игре:
  1. Длинная труба + насос + резервуар → давление и flow стабильны
  2. Развилка → корректное распределение
  3. Газовая утечка / разрыв высокодавящей трубы → блоки/газ в мире, нет пустых TE
  4. Unload/load чанка → нет потерь, нет дублирования
- Все кастомные флюиды зарегистрированы и видимы в creative

---

## JEI (Just Enough Items) — замена NEI

В 1.12.2 используется JEI вместо NEI. Мягкая зависимость.

### Структура
- `com/hbm/handler/jei/HBMJEIPlugin.java` — главный плагин (`@JEIPlugin` + `IModPlugin`)
- API jar: `libs/jei_1.12.2-4.15.0.297-api.jar` (vendored локально; загружен через curl, не через Gradle — Maven-зеркала таймаутят Gradle-загрузчик)
- `build.gradle`: `compileOnly fileTree(dir: 'libs', include: 'jei_*.jar')`
- Для запуска с JEI: положить полный JEI jar в `run/mods/`

### Соответствие NEI → JEI
| NEI (1.7.10) | JEI (1.12.2) |
|---|---|
| `implements IConfigureNEI` | `@JEIPlugin` + `implements IModPlugin` |
| `API.hideItem(stack)` | `registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(stack)` |
| `API.registerRecipeHandler(h)` + `API.registerUsageHandler(h)` | `registry.addRecipes(list, categoryUid)` + `registry.addRecipeCatalyst(item, categoryUid)` |
| `TemplateRecipeHandler` | `IRecipeCategory<W>` где `W implements IRecipeWrapper` |
| `NEIRegistry.listAllHandlers()` | Per-machine категории в `HBMJEIPlugin.register()` |

### Ключевые API классы
- `mezz.jei.api.IModPlugin` — интерфейс плагина
- `mezz.jei.api.ingredients.IIngredientBlacklist` — скрытие айтемов (через `getJeiHelpers().getIngredientBlacklist()`)
- `mezz.jei.api.recipe.IRecipeCategory` — категория рецептов для одного типа машины
- `mezz.jei.api.recipe.IRecipeWrapper` — обёртка одного рецепта для рендера

### Стратегия портирования машинных категорий
Для каждой машины при порте её TileEntity:
1. Создать `com/hbm/handler/jei/<MachineName>Category.java` реализующий `IRecipeCategory`
2. Добавить в `HBMJEIPlugin.registerCategories()` и `register()`
3. Добавить `registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_xxx), CATEGORY_UID)`

### Что ещё нужно доделать в HBMJEIPlugin
- Полное скрытие `ingot_metal` по meta когда портирован `EnumIngotMetal`
- Полное скрытие `item_secret` / `ammo_secret` по meta когда портирован GunFactory
- Скрытие memory battery стеков когда портирован `ItemBattery`
- Скрытие celestial bedrock ore grades когда портирован `ItemBedrockOreNew`
- Восстановить conditional hide по `MainRegistry.polaroidID` когда это поле будет добавлено

---

## Система плавки/литья (Foundry / Casting)

Отдельная подсистема — **не** pipe-флюиды (`Fluids.*`). Машины типа Foundry принимают твёрдые айтемы (слитки, самородки, блоки) и выдают расплавленный металл в своих внутренних единицах. Эта жидкость существует только внутри машин-плавильщиков — в трубы и резервуары общего назначения она не идёт.

### Где это в оригинале
- Рецепты — `com/hbm/inventory/RecipesFoundry.java` (или аналог, искать `FoundryRecipe`)
- Машины: Foundry, Arc Furnace, Industrial Blast Furnace — смотреть TileEntity с `melt`/`liquid`/`fluid` полями
- Айтемы-результаты — те же `ingot_*`, `plate_*`, но источник — машина, а не печь

### Правила порта
1. **Коэффициенты — только из оригинала.** Не угадывать «1 nugget = 16 mB» и т.п. Автор использует нестандартные соотношения (не 9:1 nugget:ingot в mB). Перед портом рецептов — прочитать весь `RecipesFoundry` и выписать таблицу.
2. Эта жидкость хранится как `int`/`long` внутри TE, не как `FluidStack`. Тип не менять.
3. Соотношение между самородком, слитком, блоком и пластиной — разное для разных металлов (некоторые дают дробные значения через округление). Сохранять точно.
4. При порте TileEntity плавильной машины — рецепты переносить одновременно с машиной, не «потом».

### Когда портировать
На этапе **TileEntity — простые** (плавильни, дуговые печи). К регистрации айтемов не относится.

---

## Система брони HBM (ArmorFSB и производные)

Кастомные классы брони с hazard-классами, radiation resistance, potion effects, power-системой.

### Ключевые классы (оригинал)
- `ArmorFSB` — базовый класс почти всей кастомной брони; методы: `cloneStats()`, `addEffect()`, `setSealed()`, `setHazardClass()`, `setRadResist()`, `hides(EnumPlayerPart)`
- `ArmorHazmat`, `ArmorT51`, `ArmorDesh`, `ArmorBJ`, `ArmorHEV` и т.д. — наследники ArmorFSB
- `ArmorUtil` — константы hazard-классов (`FULL_PACKAGE`, `FULL_NO_LIGHT`, …)
- `IArmorDisableModel.EnumPlayerPart` — части тела, скрываемые бронёй

### Правила порта
1. Все методы `ArmorFSB` — части игровой механики (радиозащита, скафандр-герметичность). Не выбрасывать.
2. `cloneStats()` копирует hazard/radiation/potion параметры от шлема к остальным частям. Логику сохранить.
3. `aMatXxx.customCraftingMaterial` — нет прямого аналога в 1.12.2, использовать Forge `setRepairItem()` на `ItemArmor` или хранить отдельно.
4. Power-броня (T51, BJ, DNS) имеет энергобуфер — `long`, не `int`.
5. Броня со скафандром (`setSealed(true)`) взаимодействует с системой атмосферы/космоса — портировать одновременно со SpaceConfig.

### Когда портировать
Отдельная задача после базовых TileEntity. Начинать с `ArmorFSB` как базового класса, затем по наследникам.

---

## КРИТИЧНО: Конфиг

Конфиг — часть UX мода. Сервероводы крутят эти параметры; имена опций должны переноситься копи-пастой. Игрок ожидает увидеть знакомый ему файл с теми же опциями и значениями.

В оригинале **20 файлов** в `com/hbm/config/`, **три разные системы конфига**. Не путать.

### Система 1 — Forge `Configuration` API → один файл `config/hbm.cfg`

11 классов с методом `loadFromConfig(Configuration config)`, ~440 опций суммарно.

| Файл | ~Опций | Зона |
|---|---|---|
| `WorldConfig` | 147 | worldgen, частоты руд |
| `GeneralConfig` | 60 | общие тогглы; внутри 3 категории — основная, `528`, `LESS BULLSHIT MODE` |
| `MobConfig` | 56 | мобы, спавн, баланс |
| `SpaceConfig` | 51 | гравитация, dimension IDs, ракеты |
| `StructureConfig` | 41 | dungeons, vaults, структуры |
| `RadiationConfig` | 28 | rad rate, fallout |
| `BombConfig` | 21 | радиус и сила всех взрывов |
| `PotionConfig` | 17 | эффекты зелий |
| `ToolConfig` | 13 | инструменты |
| `WeaponConfig` | 7 | оружие |
| `MachineConfig` | 2 | RTG decay |

`CommonConfig` — helper-класс: `createConfigInt/Double/Bool/String/IntList/StringList`. Сами же классы — POJO с `public static` полями и одним методом `loadFromConfig`.

Загрузка: `MainRegistry.loadConfig(FMLPreInitializationEvent)`, **до** регистрации блоков/айтемов/рецептов:
```java
Configuration config = new Configuration(event.getSuggestedConfigurationFile());
config.load();
GeneralConfig.loadFromConfig(config);
WorldConfig.loadFromConfig(config);
// ... остальные 9
config.save();
```

Категории нумерованы для управления сортировкой в `.cfg`-файле — в `CommonConfig`:
```
01_general, 02_ores, 03_nukes, 04_dungeons, 05_meteors, 06_explosions,
07_missile_machines, 08_potion_effects, 09_machines, 10_dangerous_drops,
11_tools, 12_mobs, 13_radiation, 14_hazard, 15_structures, 16_biomes,
17_dims, 18_pollution, 19_weapons + особые "528", "LESS BULLSHIT MODE"
```
Имена опций тоже нумерованы (`0.01_packetThreading...`, `1.42_threadedAtmospheres`, `9.00_scaleRTGPower`). **Сохранять нумерацию точно** — иначе пользовательские конфиги не подхватятся.

### Система 2 — `RunningConfig` (custom) → JSON в `config/hbmConfig/`, hot-reload через команды

2 класса, ~40 опций. Базовый класс `RunningConfig` (Gson + `ConfigWrapper<T>`).

| Файл | ~Опций | Зона | JSON | Команда reload |
|---|---|---|---|---|
| `ClientConfig` | 28 | HUD, рендер, GUN_*, audio | `hbmClient.json` | `/ntmclient` |
| `ServerConfig` | 11 | damage, mines, crates | `hbmServer.json` | `/ntmserver` |

Поля — `public static ConfigWrapper<T> NAME = new ConfigWrapper<>(default)`, типы T: Boolean/Integer/Float/Double/String. `initDefaults()` кладёт всё в `configMap`, `RunningConfig` сериализует в JSON и читает обратно. Инициализация через `XxxConfig.initConfig()` — вызывается в `MainRegistry` (для клиента — после `FMLInitializationEvent`).

**Hot-reload — только через игровые команды (`/ntmclient`, `/ntmserver`), не через `ConfigChangedEvent`.** Этого ивента в оригинале нет — не добавлять.

### Система 3 — Standalone JSON в `config/hbmConfig/`

3 класса, у каждого свой `.json`, инициализируются вручную в `MainRegistry`:

| Класс | Файл | Что |
|---|---|---|
| `MachineDynConfig` | `hbmMachines.json` | автогенерируется из всех TE, реализующих `IConfigurableMachine`; недостающие опции дополняются default-ами при старте |
| `FalloutConfigJSON` | `hbmFallout.json` (+ template `_hbmFallout.json`) | fallout-таблицы — что во что превращается после ядерного |
| `ItemPoolConfigJSON` | `hbmItemPools.json` (или аналог) | лут-пулы для крейтов; reload через `/reloadrecipes` |
| `CustomMachineConfigJSON` | свой формат | кастомные машины (формат специфический, читать класс целиком перед портом) |

Паттерн **template + active**: при первом старте пишется `_hbm*.json` (template, не используется в игре). Игрок копирует в `hbm*.json` (без `_`) и редактирует. Сохранять этот паттерн.

`IConfigurableMachine` — интерфейс для TE, через который машина попадает в `hbmMachines.json` (энергобуферы, скорость, расход — всё через него). Если TE в оригинале реализует — переносить с реализацией одновременно. Если не реализует — **не добавлять**, оставлять как в оригинале.

### Система 4 — Машинные рецепты `SerializableRecipe` → JSON в `config/hbmRecipes/`

**Все** машинные рецепты (плавильни, центрифуги, прессы, реакторы, реакции — 50+ типов) хранятся как JSON в **отдельной** папке `config/hbmRecipes/`, не в `hbmConfig/`. Это четвёртая система конфига и одна из главных точек кастомизации модпаков.

База — `com/hbm/inventory/recipes/loader/SerializableRecipe.java` (абстрактный класс) и `GenericRecipes` (наследник, для большинства машин).

Каждый класс рецептов в `com/hbm/inventory/recipes/`:
- наследует `SerializableRecipe` (или `GenericRecipes`)
- имеет `public static final XxxRecipes INSTANCE`
- определяет `getFileName()` → `hbmAssemblyMachine.json`, `hbmCentrifuge.json`, `hbmBlastFurnace.json` и т.д.
- в `registerDefaults()` строит дефолтный список рецептов кодом (Java)
- регистрируется в `SerializableRecipe.registerAllHandlers()`

Поведение при старте (`SerializableRecipe.initialize()`):
1. Создать `config/hbmRecipes/` если нет
2. Для каждого handler-а:
   - если `config/hbmRecipes/hbmFoo.json` **существует** → читать оттуда, `modified = true` (загружен кастомный рецепт)
   - если **нет** → запустить `registerDefaults()` (Java-код), записать `_hbmFoo.json` (template)
3. Игрок переименовывает `_hbmFoo.json` → `hbmFoo.json`, редактирует, перезагружает мод/`/reloadrecipes`

Дополнительно `SerializableRecipe.recipeSyncHandlers` — серверные рецепты могут синкаться на клиент (`GeneralConfig.enableServerRecipeSync`).

В папку также автоматически кладётся служебный файл с именем-инструкцией `REMOVE UNDERSCORE TO ENABLE RECIPE LOADING - RECIPES WILL RESET TO DEFAULT OTHERWISE` — это feature, не баг, переносить.

**Правила порта**:
- Каждый класс из `inventory/recipes/` переносится **вместе** с его машиной (TileEntity), не пакетом «все рецепты сразу»
- `getFileName()` — точно как в оригинале, иначе модпаки сломаются
- `registerDefaults()` переносить **дословно** — это и есть исходник рецептов (если игрок не правил JSON)
- Не превращать в Forge JSON-recipes (`assets/<modid>/recipes/`) — это другая система, для верстака
- При порте машины **сначала** перенести её `XxxRecipes` (с дефолтами в коде) — **потом** TileEntity, который их потребляет
- Vanilla shaped/shapeless рецепты (верстак) живут отдельно — `com/hbm/main/CraftingManager.java`, **другая** система переноса (Forge JSON в `assets/hbm/recipes/*.json` или `GameRegistry.addShapedRecipe` в коде)

---

## Логгирование (анти-гриферская система)

Серверный must-have. Один флаг `GeneralConfig.enableExtendedLogging` (опция `1.18_enableExtendedLogging` в `hbm.cfg`, default `false`) включает логи для **39 файлов** — всё, чем можно нагриферить: бомбы, ядерки, гранаты, мины, ракеты, детонаторы, ломающие предметы. Админы серверов вылавливают гриферов через эти логи.

### Префиксы (стандартизированы, переносить точно)

| Префикс | Что | Где (примеры) |
|---|---|---|
| `[BOMBPL]` | placement бомбы/нюки/мины | `blocks/bomb/Nuke*.java`, `Bomb*.java`, `BlockTNT`, `BlockDynamite`, `BlockSemtex`, `BlockFissureBomb`, `BlockVolcano`, `BlockFireworks`, `Landmine`, `ExplosiveCharge` |
| `[NUKE]` | инициализация ядерного взрыва | `entity/logic/EntityNukeExplosionMK3`, `EntityNukeExplosionMK5`, `EntityBalefire`, `EntityTomBlast` |
| `[DET]` | использование детонатора | `items/tool/ItemDetonator`, `ItemLaserDetonator`, `ItemMultiDetonator`, `items/special/ItemDrop` (dead man's switch), `main/ModEventHandler` (lever) |
| `[GREN]` | гранаты | `entity/grenade/EntityGrenadeBase`, `EntityGrenadeBouncyBase`, `EntityGrenadeUniversal` |
| `[MISSILE]` | запуск ракеты | `tileentity/bomb/TileEntityLaunchPadBase` |
| `[MKU]` | MKU-шприц | `items/special/ItemSyringe` |

### Формат сообщения (точно как в оригинале)

**Placement бомбы:**
```java
if(GeneralConfig.enableExtendedLogging) {
    MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! by " + player.getCommandSenderName());
}
```
**Детонатор:**
```java
if(GeneralConfig.enableExtendedLogging)
    MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");
```
**Ядерный взрыв (формат с `{}` через варарги):**
```java
MainRegistry.logger.log(Level.INFO, "[NUKE] Initialized explosion at {} / {} / {} with strength {}!", x, y, z, r);
```

### Правила порта

1. `Level` — это `org.apache.logging.log4j.Level`, не `java.util.logging.Level`. Импорт сохранять
2. `MainRegistry.logger` — экземпляр Log4j2, в 1.12.2 формат тот же
3. **1.7.10 → 1.12.2 замены** в логирующем коде:
   - `player.getDisplayName()` — в 1.12.2 возвращает `ITextComponent`, нужен `.getFormattedText()` или использовать `player.getName()` (String)
   - `player.getCommandSenderName()` — удалён, использовать `player.getName()`
   - `xCoord`/`yCoord`/`zCoord` (TE) → `pos.getX()`/`getY()`/`getZ()`
4. Сообщение — **дословно**, включая отсутствие пробела после `[BOMBPL]` и одинарные восклицательные знаки. Серверные парсеры написаны под этот формат
5. Проверка `if(GeneralConfig.enableExtendedLogging)` — не выносить в helper, оставить inline (как в оригинале), иначе пропадёт ветка-предсказание JIT и в hot path будет лишний вызов
6. При порте каждой бомбы/гранаты/ракеты/детонатора — **сразу** переносить и лог-вызов в том же коммите, не «потом»
7. Хорошее место под smoke-тест после миграции: с `enableExtendedLogging=true` поставить нюк, детонировать, бросить гранату, запустить ракету → проверить что в `logs/latest.log` появились все 4 префикса с координатами и ником

Логи попадают в стандартный `logs/latest.log` сервера. Отдельного файла лога мод не пишет.

### Не файл конфига, но в той же папке

`VersatileConfig` — helper с runtime-логикой поверх флагов `enable528`/`enableLBSM` (`getTransmutatorItem()`, `getSchrabOreChance()`, `applyPotionSickness()`). Не путать с настоящими конфигами; переносить после `GeneralConfig`+`PotionConfig`+`MachineConfig`, от которых зависит.

### Правила порта

1. **Сохранить структуру файлов и имена опций** (включая числовые префиксы `01_`, `0.01_`) — чтобы серверные конфиги переносились копи-пастой
2. Сохранить все опции, даже на первый взгляд бесполезные (легаси-флаги нужны для совместимости старых конфигов)
3. Сохранить дефолты 1:1, в том числе все капризы вроде `enableLBSMShorterDecay = true` при `enableLBSM = false`
4. Категории и комментарии (включая `addCustomCategoryComment` с многострочными warning-ами) переносить дословно — это документация для админов
5. JSON-конфиги — формат точь-в-точь, включая template-паттерн (`_hbm*.json`)
6. Использовать Forge `Configuration` API для системы 1, не `@Config` аннотации — ближе к оригиналу
7. Hot-reload — **только** для систем 2/3 через `/ntm*` команды и `/reloadrecipes`. **Не добавлять** `ConfigChangedEvent` — оригинал его не использует
8. Порядок инициализации в `MainRegistry`:
   - Создать `configHbmDir = new File(configDir, "hbmConfig")`, `mkdir()` если нет
   - В `FMLPreInitializationEvent` → `loadConfig()` (Forge `Configuration`) — **до** регистрации блоков/айтемов
   - В `FMLInitializationEvent` (или подходящем месте) → `MachineDynConfig.initialize()`, `FalloutConfigJSON.initialize()`, `ItemPoolConfigJSON.initialize()`, `CustomMachineConfigJSON.initialize()`, `ClientConfig.initConfig()`, `ServerConfig.initConfig()`
9. Системы 2/3 пишут в `config/hbmConfig/`, не в корень `config/`

### Контрольный список (что обязательно конфигурируемо)
- Радиус и сила всех взрывов (`BombConfig`)
- Скорость генерации/затухания радиации, fallout (`RadiationConfig`, `FalloutConfigJSON`)
- Recipe overrides — включение/выключение целых категорий (`MachineDynConfig`, `MachineConfig`, `GeneralConfig.enableLBSM*`)
- Параметры RBMK и реакторов (через `IConfigurableMachine` → `hbmMachines.json`)
- Worldgen rates: руды, структуры, метеоры (`WorldConfig`, `StructureConfig`)
- Параметры сетей: throughput, лимиты обхода
- Параметры космоса: гравитация, ресурс топлива, скорость ракет, dim IDs (`SpaceConfig`)
- Враги и боссы (`MobConfig`)
- Оружие и инструменты (`WeaponConfig`, `ToolConfig`)
- HUD-оффсеты, рендер-флаги, аудио — клиент-сторона (`ClientConfig`)
- Damage profile, мины, тайнт — сервер-сторона (`ServerConfig`)
- Per-machine энергобуфер/потребление (`hbmMachines.json` через `IConfigurableMachine`)
- Лут-пулы крейтов и кастомные fallout-таблицы (JSON-конфиги)

При порте механики с конфигом в оригинале — переносить опции одновременно с кодом.

---

## КРИТИЧНО: Космос и планеты (форк James)

Самая сложная и хрупкая часть. Добавляется последней, после стабильных: блоков, айтемов, конфига, сетей, базовых TE.

### Где это в оригинале
- `com/hbm/dim/` — измерения и планеты (имена в духе KSP):
  - Земля: `WorldProviderEarth`
  - Планеты: `moon`, `duna`, `eve`, `laythe`, `minmus`, `moho`, `dres`, `Ike`
  - Каркас: `CelestialBody`, `SolarSystem`, `WorldProviderCelestial`, `ChunkProviderCelestial`, `BiomeGenBaseCelestial`, `BiomeDecoratorCelestial`, `WorldGeneratorCelestial`, `CelestialTeleporter`, `SkyProviderCelestial`, `WorldChunkManagerCelestial`
  - Сейв-данные: `SolarSystemWorldSavedData`, `CelestialBodyWorldSavedData`
- Конфиг: `com/hbm/config/SpaceConfig.java`
- Орбитальная станция и космические структуры — `com/hbm/dim/` и `com/hbm/world/` (искать «orbit», «station»)

### Правила порта
1. `DimensionType` vs `DimensionRegistry`: в 1.12.2 регистрация — `DimensionType` + `DimensionManager.registerDimension(id, type)`. Не путать с 1.7.10
2. Иерархия `WorldProvider` поменялась — методы переименованы/удалены, проверять каждый `@Override`
3. `Teleporter` API другой — межизмеренческий перенос Entity сложнее. См. `placeInPortal` или кастомный поток
4. Фиксированные ID измерений — если ID не зафиксирован в `SpaceConfig`, миры с орбитальной станцией ломаются после рестарта
5. `SkyProvider` и `CloudProvider` — рендерные интерфейсы изменились
6. Совместимость с другими мир-моддерами (Galacticraft и т.п.) — не лезть в их dimension ID
7. Чанкбаны на орбитальной станции — частая проблема. Особо строго применять боилерплейт TE

### Стратегия (порядок шагов)
1. Worldgen + `WorldProviderCelestial` для одной планеты (Луна) без ракет — попадание через дев-команду
2. Ракета как Entity с GUI выбора, без анимации полёта (мгновенный teleport)
3. Атмосфера + гравитация + скафандр (взаимосвязанные системы)
4. Анимация запуска ракеты, эффекты
5. Орбитальная станция как отдельное измерение с фиксированной структурой
6. Остальные планеты по одной
7. Стресс-тест на каждом шаге: туда-обратно 50 раз без рестарта сервера

### Definition of done
- BUILD SUCCESSFUL
- Туда-обратно 50 раз — нет утечек, нет дублирования entity, нет роста потребления памяти
- `SpaceConfig` перенесён
- Скафандр и атмосфера работают (без скафандра HP падает на планетах с вакуумом)

### Предусловие
Не браться за космос пока не закрыты: блоки, айтемы, конфиг, энерго- и флюидные сети, базовые TE.

При начале — прочитать `com/hbm/dim/` целиком, выписать словесную карту зависимостей классов (Body → Provider → ChunkProvider → BiomeDecorator → Teleporter), потом писать код.

---

## Сложные механики (карта оригинала)

Это подсистемы, не такие приоритетные как сети/конфиг/космос, но требующие отдельного внимания при порте: специфическая физика, нелинейная архитектура, или общеизвестно багованные. **Перед задачей читать соответствующий пункт.**

### Оружие, патроны, гранаты, анимация
- Айтемы оружия: `com/hbm/items/weapon/` (стрелковое, гранаты, ракеты, миномёты), `com/hbm/items/special/grenade/`, `com/hbm/items/ModItemsArmor.java`
- Конфиг и баллистика: `com/hbm/handler/BulletConfiguration.java`, `BulletConfigSyncingUtil.java`, `com/hbm/handler/guncfg/*`
- Интерфейсы анимации/контроля (в `com/hbm/items/`): `IAnimatedItem`, `IItemControlReceiver`, `IKeybindReceiver`, `IHeldSoundProvider`, `IEquipReceiver`
- Кастомный рендер стрелкового — самая хрупкая часть. В 1.12.2 рендер айтемов поменялся (`IBakedModel` + perspective-aware models, `TransformType`). Анимации привязаны к `IAnimatedItem` и кастомному handler-у.
- **Порядок порта**: айтем без анимации/звука → стрельба (Entity пули, Bullet handler) → анимация → звук → keybind/control. Не пытаться всё сразу.
- Рецепты пуль и оружия — в `MachineDynConfig` или JSON, проверять перед хардкодом

### Нейтронный поток (RBMK и реакторы)
- `com/hbm/handler/neutron/`:
  - `NeutronHandler`, `NeutronNode`, `NeutronNodeWorld`, `NeutronStream` — общая система
  - `PileNeutronHandler`, `RBMKNeutronHandler` — конкретные реакторы
- Каждый стержень — узел графа; нейтроны идут от соседей. **Граф обходится каждый тик** на каждый стержень → одна из главных причин чанкбанов оригинала.
- При порте: алгоритм обхода — **точно как в оригинале**, не «оптимизировать». Добавить лимит глубины/количества узлов в одном тике (см. боилерплейт TE — 1000 узлов).
- Юнит-тесты на чистую математику нейтронного баланса — **обязательно** (см. «Математические модели»). Ошибка в формуле = реактор не взрывается когда должен или взрывается когда не должен.

### Foundry / плавильня / жидкие металлы
- TileEntity: `com/hbm/tileentity/machine/TileEntityFoundry*`:
  - `Base`, `Basin`, `CastingBase`, `Channel`, `Mold`, `Outlet`, `Slagtap`, `Tank`
  - Плюс `TileEntityCrucible` и интерфейс `IRenderFoundry`
- Материальная система: `com/hbm/inventory/material/` — `Mats`, `NTMMaterial`, `MatDistribution`, `MaterialShapes`. Это **отдельная подсистема жидких металлов**, не стандартный Forge fluid.
- Связь с флюидной сетью: используется `IFluidStandardTransceiver` — субсистема внутри общей системы труб (см. «Флюидные сети»).
- Геймплейно: тигель плавит шихту → basin/casting формирует слитки → channel переливает между секциями → mold задаёт форму на выходе.
- **Порядок порта**: материальная система (`Mats`, `NTMMaterial`) → тигель отдельно с unit-тестом плавления → базовый Foundry (basin + casting) → каналы и формы. Не пакетом.

### Атмосфера и герметичность
- `com/hbm/handler/atmosphere/`:
  - `AtmosphereBlob` — пузырь воздуха внутри герметичного объёма
  - `ChunkAtmosphereHandler`, `ChunkAtmosphereManager` — учёт по чанкам
  - `IAtmosphereProvider` — что блок/TE может «производить» атмосферу
  - `IBlockSealable` — блоки, держащие воздух (стены, двери)
  - `IPlantableBreathing` — растения, требующие кислород
- Логика: герметичный объём блоков → хранит атмосферу; разрушение блока на границе → утечка
- Связи: космос (планетный воздух), скафандр (потребление O₂), флюидная сеть (баллоны O₂/CO₂), радиация (fallout-газ)
- Порт — **после** блоков, после флюидной сети, **до** космоса

### Гравитация (per-dimension)
- Параметры тел: `com/hbm/dim/CelestialBody.java` — поле гравитации каждой планеты
- Орбитальная станция: `com/hbm/dim/orbit/OrbitalStation.java`
- Применяется через `WorldProvider`-ы планет; модификатор скорости падения Entity по dim-у
- В оригинале гравитация **багованная** — это известно. При порте: переносить как есть, **не пытаться чинить**. Фиксы — отдельная задача после полного порта по запросу пользователя.

### Радиация и fallout
- `com/hbm/handler/radiation/`, `com/hbm/handler/pollution/`
- `com/hbm/config/RadiationConfig.java`, `FalloutConfigJSON`
- Уже частично покрыто в боилерплейт TE (бюджет ≤ 1 чанк/тик) и «Математические модели». Перечитать перед портом.

---

## Тестирование

### Контрольные точки (когда деплоить)
1. После регистрации всех блоков
2. После регистрации всех айтемов
3. После полного конфига
4. После визуала (model JSON + blockstate JSON + lang)
5. После каждой машины с TileEntity
6. После каждой сети (энерго, флюид)
7. После каждого нового измерения

### Smoke-тест после регистрации
1. `gradlew build` → BUILD SUCCESSFUL
2. Скопировать `build/libs/*.jar` на VPS в `mods/`
3. Сервер стартует без `FATAL`/`ERROR` в логе
4. Подключиться клиентом
5. `/give @p hbm:<sample>` — не краш
6. Поставить блок — не краш
7. Открыть Creative tab — блоки видны (розовые без текстур = норм пока)

### Что ловить в логах
- `Duplicate block/item registry name` → двойная регистрация
- `NullPointerException` в init → поле не проинициализировано
- `Registry entry not found` → ItemBlock без Block или наоборот
- `Unknown block/item` → неправильный registry name
- `Mismatched dimension data` → проблемы с DimensionType (этап космоса)

### Definition of done для задачи в целом
Перед заявлением «готово»:
- `gradlew build` → BUILD SUCCESSFUL
- Сервер стартует без FATAL/ERROR
- Smoke-тест пройден (если применим)
- Stress-тесты пройдены (для критичных механик)
- Соответствующий конфиг перенесён
- Клиент И сервер оба запускаются (CONTRIBUTING требует)
- Пункт в «Статус порта» отмечен ✓

На каждой контрольной точке — явно сказать пользователю: «Деплоим и проверяем. Тестируем: [список конкретных команд и действий]».

---

## Математические модели

В HBM много физики (взрывы, нейтронный поток, радиация, термодинамика). Ошибка в формуле = другие числа = другой геймплей.

Перед портом матмодели:
1. Изолировать чистую математику в класс без зависимостей от Minecraft
2. Юнит-тесты в `src/test/java/...` с фиксированными референс-значениями из 1.7.10
3. Сравнение в игре стресс-тестом

Грабли: `int`/`long` overflow, деление на ноль (особенно `crossSection = 0` в нейтронной физике), `float` vs `double`. Использовать тот же тип, что в оригинале.

---

## Система оружия «Sedna» (GunFactory) — портировать последней

Самая сложная после космоса. ~90 файлов в `com/hbm/items/weapon/sedna/`. Не трогать до тех пор, пока не закрыты: блоки, айтемы, конфиг, сети, базовые TE, рендер.

### Архитектура
Все огнестрельное оружие — экземпляры **одного класса** `ItemGunBaseNT`. Поведение задаётся через data-объекты и Java 8 lambdas:

```
ItemGunBaseNT + GunConfig + Receiver(s) + IMagazine + BulletConfig + Lego-lambdas
```

**GunFactory.init()** — центральный инициализатор; делегирует в ~20 `XFactory*.init()` по типу калибра/механики:
`XFactory9mm`, `XFactory12ga`, `XFactory762mm`, `XFactoryFlamer`, `XFactoryEnergy`, `XFactoryDrill`, `XFactoryPA`, …

### Что регистрирует
- `ammo_standard` — `ItemEnumMulti(EnumAmmo)`, ~100+ патронов как мета-значения
- `ammo_secret` — `ItemEnumMulti(EnumAmmoSecret)`, 8 секретных патронов (в creative скрыты)
- `weapon_mod_generic/special/caliber/test` — мета-айтемы модификаций оружия
- ~55 `gun_*` полей в `ModItems` — каждый `new ItemGunBaseNT(quality, GunConfig)`

Пока все эти поля зарегистрированы как `new Item()` заглушки — сервер не падает.

### Ключевые классы
| Класс | Роль |
|---|---|
| `ItemGunBaseNT` | Единственный item-класс всех пушек |
| `GunConfig` | Параметры: durability, draw speed, receivers |
| `Receiver` | Режим огня: урон, delay, reload, jam, звук, magazine |
| `IMagazine` и наследники | Механика перезарядки (FullReload, Belt, Fluid, …) |
| `BulletConfig` | Пуля: spread, projectiles, ricochet, гильза |
| `Lego` | Стандартные lambda-константы для fire/reload/aim |
| `GunStateDecider` | FSM переходов состояний (draw→idle→fire→reload) |
| `XWeaponModManager` | NBT-менеджер модификаций на айтеме |
| `ItemEnumMulti` | Кастомный HBM мета-айтем — портировать до Sedna |

### Зависимости (нужны до начала порта Sedna)
1. `ItemEnumMulti` — кастомный класс, нужен для `ammo_standard`, `weapon_mod_*`
2. Networking: `HbmAnimationPacket` → 1.12.2 `SimpleNetworkWrapper`
3. Keybinds: `cpw.mods.fml.*` → `net.minecraftforge.fml.*`, ForgeKeyBinding API
4. Рендер анимаций: `GunAnimation`, `RenderScreenOverlay` — рендер pipeline другой в 1.12.2
5. WeaponTable GUI (`GUIWeaponTable`) — GUI для установки модов

### Правило
Не начинать до тех пор, пока в статусе не отмечены: TileEntity (простые), энергосети, флюидные сети, рендер. Sedna — отдельный PR.

---

## Легаси
- Часть блоков/айтемов — заглушки и недоделки оригинала
- Часть механик есть в коде, но не реализована
- Не удалять самостоятельно — только вместе с пользователем
- При переносе — перенести как есть, пометить `// LEGACY?` если явно мертво
- Финальная чистка — отдельный этап после полного порта

---

## Хеш-пазл
В моде есть таблички/таблицы где игрок вводит ответы, код сравнивает хеши. Обфусцировано намеренно.
- Не пытаться взломать хеши
- Перенести алгоритм и захардкоженные хеши точно как в оригинале
- Java `String.hashCode()` стабилен между JVM
- Перед началом — попросить у пользователя оригинальный код механики

---

## Код-ревью (внешние инструменты)
- **SpotBugs** (`com.github.spotbugs` Gradle plugin, `gradlew spotbugsMain`) — после `initBlocks` + `initItems`
- **IntelliJ inspections** — перед первыми TileEntity
- **SonarCloud** (через GitHub Actions) — перед каждым PR

До заполнения `initBlocks`+`initItems` анализ слишком шумный, не подключать.

---

## Статус порта
- [x] Скелет проекта (build.gradle, основной класс, прокси) — BUILD SUCCESSFUL
- [x] Creative tabs — все 9
- [x] ModBlocks.java — 1114 полей, скелет `@EventBusSubscriber`
- [x] ModItems.java — 1948 полей, скелет регистрации
- [x] GitHub Actions CI
- [x] initBlocks() — руды, камни, кластеры, флюид-руды (~116 блоков)
- [x] initBlocks() — block_* материальные + deco_* + misc surface (~110 блоков)
- [x] initBlocks() — строительные (бетон, кирпичи, лестницы, плиты, освещение, метеориты, луна…)
- [x] initBlocks() — все оставшиеся секции (взрывчатка, машины, сети, жидкости/газы, космические, прочее) — 156 блоков добавлены скриптом, BUILD SUCCESSFUL
- [x] initItems() — 1929 айтемов зарегистрировано как new Item() заглушки; 103 поля не регистрируются: ~55 guns/ammo/weapon_mod через GunFactory (не портирован), остальные — закомментированные или нереализованные поля оригинала
- [x] JEI интеграция — `com/hbm/handler/jei/HBMJEIPlugin.java`; soft dependency (`compileOnly` из `libs/`); скрытие айтемов по аналогии NEIConfig; машинные категории добавляются по мере порта TE
- [ ] Кастомные классы блоков (`BlockOre`, `BlockOreOutgas`, …)
- [x] Конфиг Система 1 — Forge `Configuration` API: 12 файлов (`CommonConfig` + 11 классов, ~440 опций), подключено в `MainRegistry.loadConfig()`, BUILD SUCCESSFUL. Фиксы: `prop.comment` → `prop.setComment()`, `cpw.mods.fml` → `net.minecraftforge.fml`, инлайн `RefStrings.MODID`/`Compat.MOD_EIDS`, заглушки для `PrecAssRecipes`/`ChunkRadiation*`/`BiomeCollisionException`
- [x] Конфиг Система 2 — `RunningConfig` + `ClientConfig` + `ServerConfig` (~40 опций), BUILD SUCCESSFUL. Фикс: `Compat.isModLoaded(Compat.MOD_ANG)` → `Loader.isModLoaded("angelica")`; `configHbmDir` в `MainRegistry.preInit`; `ServerConfig.initConfig()` в `MainRegistry.init`; `ClientConfig.initConfig()` в `ClientProxy.init`
- [x] Конфиг Система 3 — инфраструктура JSON-конфигов: `MachineDynConfig` + `FalloutConfigJSON` + `ItemPoolConfigJSON`, подключено в `MainRegistry.init()`, BUILD SUCCESSFUL. `CustomMachineConfigJSON` пропущен (специфический формат). Стабы под порт: `NotableComments`, `IConfigurableMachine`, `TileMappings` (пустой `configurables`), `Tuple` (Pair+Triplet), минимальный `RecipesCommon` (только `MetaBlock`), минимальный `Compat` (`MOD_EF`+`tryLoadBlock`), `ItemPool` с собственным `WeightedEntry` (вместо удалённого ванильного `WeightedRandomChestContent`). Фиксы: `Block.blockRegistry.getObject(s)` → `Block.REGISTRY.getObject(new ResourceLocation(s))`; `Material.*`/`Blocks.*` → UPPERCASE; `world.setBlock(...)` → `world.setBlockState(pos, b.getStateFromMeta(m), 3)`; `b.getMaterial()`/`b.isOpaqueCube()` → лениво через `IBlockState` (публичная сигнатура `eval` сохранена); `stack.stackSize`/`stackTagCompound` → `getCount()`/`getTagCompound()`/`setTagCompound`; `JsonToNBT.func_150315_a` → `JsonToNBT.getTagFromJson`; `WeightedRandomChestContent` → `ItemPool.WeightedEntry`
- [ ] Конфиг Система 4 — `SerializableRecipe` (машинные рецепты, переносится с каждой машиной)
- [ ] Визуал (model JSON, blockstate JSON, lang)
- [ ] TileEntity — простые
- [ ] **Энергосети** (см. раздел)
- [ ] **Флюидные сети** (см. раздел)
- [ ] TileEntity — сложные (RBMK, реакторы)
- [ ] Рецепты
- [ ] Worldgen
- [ ] Рендеринг
- [ ] **Система оружия Sedna / GunFactory** (см. раздел — одна из последних задач)
- [ ] **Космос и планеты** (см. раздел — последнее)

---

## Для новых сессий
1. Прочитай этот файл целиком, особенно «Принципы порта», «Декомпозиция и контроль больших задач» и «Что переносить осторожно».
2. Конкретную задачу формулируй: «Переносим [механика] — исходник: [путь]».
3. Если задача затрагивает энергосети, флюидные сети, конфиг или космос — сначала читать соответствующий КРИТИЧНО-раздел.
4. Если задача затрагивает оружие/анимацию, нейтронный поток (RBMK), Foundry/плавильню, атмосферу или гравитацию — сначала читать «Сложные механики».
5. Если задача большая (>20 однотипных операций, или >5 файлов, или >200 строк) — **сначала план в чат**, потом код по этапам с чекпойнтами.
6. Перед написанием кода — Read оригинального файла из 1.7.10 целиком.
7. Перед «готово» — пройти Definition of done.
