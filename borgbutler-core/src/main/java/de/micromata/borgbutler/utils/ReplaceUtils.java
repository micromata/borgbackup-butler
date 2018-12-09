package de.micromata.borgbutler.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ReplaceUtils {
    public static final String ALLOWED_FILENAME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._-";
    public static final String PRESERVED_FILENAME_CHARS = "\"*/:<>?\\|";
    public static final char FILENAME_REPLACE_CHAR = '_';

    private static Map<Character, String> umlautReplacementMap;

    static {
        umlautReplacementMap = new HashMap<>();
        umlautReplacementMap.put('Ä', "Ae");
        umlautReplacementMap.put('Ö', "Oe");
        umlautReplacementMap.put('Ü', "Ue");
        umlautReplacementMap.put('ä', "ae");
        umlautReplacementMap.put('ö', "oe");
        umlautReplacementMap.put('ü', "ue");
        umlautReplacementMap.put('ß', "ss");
    }

    /**
     * Preserved characters (Windows): 0x00-0x1F 0x7F " * / : < > ? \ |
     * Preserved characters (Mac OS): ':'
     * Preserved characters (Unix): '/'
     * Max length: 255
     *
     * @param filename
     * @param reducedCharsOnly if true, only {@link #ALLOWED_FILENAME_CHARS} are allowed and German Umlaute are replaced
     *                         'Ä'->'Ae' etc. If not, all characters excluding {@link #PRESERVED_FILENAME_CHARS} are allowed and
     *                         all white spaces will be replaced by ' ' char.
     * @return
     */

    public static String encodeFilename(String filename, boolean reducedCharsOnly) {
        if (StringUtils.isEmpty(filename)) {
            return "file";
        }
        if (reducedCharsOnly) {
            filename = replaceGermanUmlauteAndAccents(filename);
        }
        StringBuilder sb = new StringBuilder();
        char[] charArray = filename.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char ch = charArray[i];
            if (reducedCharsOnly) {
                if (ALLOWED_FILENAME_CHARS.indexOf(ch) >= 0) {
                    sb.append(ch);
                } else {
                    sb.append(FILENAME_REPLACE_CHAR);
                }
            } else {
                if (ch <= 31 || ch == 127) { // Not 0x00-0x1F and not 0x7F
                    sb.append(FILENAME_REPLACE_CHAR);
                } else if (PRESERVED_FILENAME_CHARS.indexOf(ch) >= 0) {
                    sb.append(FILENAME_REPLACE_CHAR);
                } else if (Character.isWhitespace(ch)) {
                    sb.append(' ');
                } else {
                    sb.append(ch);
                }
            }
        }
        String result = sb.toString();
        if (result.length() > 255) {
            return result.substring(0, 255);
        }
        return result;
    }

    public static String replaceGermanUmlauteAndAccents(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        char[] charArray = text.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char ch = charArray[i];
            if (umlautReplacementMap.containsKey(ch)) {
                sb.append(umlautReplacementMap.get(ch));
            } else {
                sb.append(ch);
            }
        }
        return StringUtils.stripAccents(sb.toString());
    }
}
