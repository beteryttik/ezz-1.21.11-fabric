package com.ezzclient.config;

import java.util.HashMap;
import java.util.Map;

public class LangManager {
    public static String language = "English";
    public static final String[] LANGUAGES = {"English", "Russian"};

    private static final Map<String, Map<String, String>> DICT = new HashMap<>();
    static {
        Map<String, String> en = new HashMap<>();
        en.put("search", "Search");
        en.put("enabled", "ENABLED");
        en.put("disabled", "DISABLED");
        en.put("options", "OPTIONS");
        en.put("newConfig", "NEW CONFIG");
        en.put("editHud", "EDIT HUD LAYOUT");
        en.put("remove", "REMOVE");
        en.put("dragToRotate", "SELECT TO EQUIP");
        en.put("noResults", "Nothing found");
        DICT.put("English", en);

        Map<String, String> ru = new HashMap<>();
        ru.put("search", "Поиск");
        ru.put("enabled", "ВКЛЮЧЕНО");
        ru.put("disabled", "ВЫКЛЮЧЕНО");
        ru.put("options", "НАСТРОЙКИ");
        ru.put("newConfig", "НОВЫЙ КОНФИГ");
        ru.put("editHud", "РЕДАКТИРОВАТЬ HUD");
        ru.put("remove", "СНЯТЬ");
        ru.put("dragToRotate", "ВЫБЕРИ ДЛЯ ЭКИПИРОВКИ");
        ru.put("noResults", "Ничего не найдено");
        DICT.put("Russian", ru);
    }

    public static String t(String key) {
        Map<String, String> d = DICT.getOrDefault(language, DICT.get("English"));
        return d.getOrDefault(key, key);
    }
    public static void cycle(int dir) {
        int i = java.util.Arrays.asList(LANGUAGES).indexOf(language);
        language = LANGUAGES[(i + dir + LANGUAGES.length) % LANGUAGES.length];
        ConfigManager.save();
    }
}
