# Ezz Client 2.0 — Fabric 1.21.11

Клиентский мод в стиле Phaze Client: косметика, HUD-модули, анимации и буст FPS.
**Вся косметика и визуал — только для тебя**, другие игроки видят обычный скин.

## Что внутри

- **31 модуль**: FPS, координаты, пинг, CPS, keystrokes, Armor HUD, Health, Potion List,
  Minimap, Watermark, Fake FPS, Full Bright, Zoom, Custom Sky Color, World Particles,
  Animations, Old Animations, Motion Blur, Crosshair Custom, Nametag Custom, Time Changer,
  Auto Walk/Sprint/Eat/GG/Respawn, Death Coords, Notifications, Chat Filter, Performance и др.
- **Косметика**: 8 капов, 6 крыльев (заменяют элитры — `HIDE ELYTRA`), 8 шляп,
  6 петов, включая **Skin Clone** — мини-клон ТВОЕГО скина у плеча. Анимация маха крыльев
  регулируется слайдером WING MOTION.
- **GUI в стиле Phaze**: вкладки MODS/SETTINGS/COSMETICS/CONFIGS, карточки с иконками,
  живой поиск, фильтры категорий, конфиги-профили, редактор HUD (перетаскивание мышью),
  6 тем, EN/RU.

## Сборка

Нужен JDK 21 и Gradle 8.14+ (или запусти `gradle wrapper`, затем `./gradlew build`).

```
gradle build
```

Готовый jar появится в `build/libs/ezzclient-2.0.0.jar`. Положи его в `.minecraft/mods`
вместе с **Fabric API для 1.21.11** и Fabric Loader 0.18.4+.

## Управление

- **RShift** — открыть/закрыть GUI (можно поменять в настройках Minecraft: "Ezz Client")
- **Z** — зум (модуль Zoom)

## Честные оговорки

1. Проект написан под маппинги Yarn 1.21.11+build.4. В рендере косметики
   (CosmeticFeature, WingsModel, ElytraFeatureMixin, PetRenderer) используются свежие
   сигнатуры 1.21.11 — если `gradle build` выдаст ошибку компиляции, пришли её текст:
   правится точечно за пару минут.
2. Performance-модуль даёт честный прирост (туман/облака/частицы/дистанция), но
   «гарантированные 60→150» обещать нельзя: зависит от Sodium в сборке, карты и железа.
   Для максимума ставь рядом Sodium + Lithium + FerriteCore + Entity Culling.
3. Discord RPC — заглушка без зависимости (lib нужен отдельный maven-репозиторий).
4. FPS Boost не содержит читов: боевых модулей (Hit Range и т.п.) в клиенте нет.

## Структура

```
src/main/java/com/ezzclient/
├── EzzClient.java          — входная точка, биндинги, события
├── module/                 — система модулей и настроек
├── modules/AllModules.java — все 31 модуль
├── config/                 — профили, темы, язык
├── gui/                    — ClickGUIScreen + HudEditorScreen
├── cosmetics/              — каталог косметики
├── cosmetics/render/       — модели крыльев, шляп, капов, пета
└── mixin/                  — замена элитр/капа, кастомный прицел
```
