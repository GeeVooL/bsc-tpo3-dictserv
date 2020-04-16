package com.mdevv.tpo3.common;

import java.util.*;

public class Languages {
    public static String DEFAULT = Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH);
    private static Map<String, String> languages = new HashMap<>();

    static {
        Arrays.stream(Locale.getAvailableLocales()).forEach(locale ->
            languages.put(locale.getDisplayLanguage(Locale.ENGLISH), locale.getLanguage().toLowerCase())
        );
        languages.remove("");
    }

    public static Map<String, String> getLanguagesMap() {
        return languages;
    }

    public static List<String> getLanguagesList() {
        List<String> list = new ArrayList<>(languages.keySet());
        list.sort(Comparator.naturalOrder());
        return list;
    }

    public static String getCode(String name) {
        return languages.get(name);
    }
}
