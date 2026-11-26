package com.lakbaylokal.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Zero-dependency JSON serializer and deserializer.
 * Supports converting Java models, maps, and collections to and from JSON.
 */
public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Date) {
            return "\"" + DateUtil.formatDateTime((Date) obj) + "\"";
        }
        if (obj instanceof Enum<?>) {
            return "\"" + ((Enum<?>) obj).name() + "\"";
        }
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj.getClass().isArray()) {
            Object[] arr = (Object[]) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(arr[i]));
            }
            sb.append("]");
            return sb.toString();
        }

        // Generic POJO reflection
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object val = field.get(obj);
                if (!first) sb.append(",");
                sb.append("\"").append(field.getName()).append("\":");
                sb.append(toJson(val));
                first = false;
            } catch (Exception ignored) {
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Parses a JSON string into a Map<String, Object>.
     */
    public static Map<String, Object> parseJsonObject(String json) {
        if (json == null) return Collections.emptyMap();
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return Collections.emptyMap();
        }
        return parseObjectInternal(json);
    }

    private static Map<String, Object> parseObjectInternal(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        int i = 1;
        int len = json.length() - 1;

        while (i < len) {
            while (i < len && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= len || json.charAt(i) == '}') break;

            if (json.charAt(i) != '"') {
                i++;
                continue;
            }
            int keyStart = ++i;
            while (i < len && (json.charAt(i) != '"' || json.charAt(i - 1) == '\\')) i++;
            String key = unescape(json.substring(keyStart, i));
            i++; // skip closing quote

            while (i < len && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ':')) i++;

            // Parse value
            int valStart = i;
            if (i >= len) break;

            char c = json.charAt(i);
            Object value = null;
            if (c == '"') {
                valStart = ++i;
                while (i < len && (json.charAt(i) != '"' || json.charAt(i - 1) == '\\')) i++;
                value = unescape(json.substring(valStart, i));
                i++;
            } else if (c == '{') {
                int depth = 1;
                valStart = i++;
                while (i < len && depth > 0) {
                    if (json.charAt(i) == '{') depth++;
                    else if (json.charAt(i) == '}') depth--;
                    i++;
                }
                value = parseObjectInternal(json.substring(valStart, i));
            } else if (c == '[') {
                int depth = 1;
                valStart = i++;
                while (i < len && depth > 0) {
                    if (json.charAt(i) == '[') depth++;
                    else if (json.charAt(i) == ']') depth--;
                    i++;
                }
                value = parseArrayInternal(json.substring(valStart, i));
            } else {
                while (i < len && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
                String rawVal = json.substring(valStart, i).trim();
                if ("null".equals(rawVal)) value = null;
                else if ("true".equalsIgnoreCase(rawVal)) value = Boolean.TRUE;
                else if ("false".equalsIgnoreCase(rawVal)) value = Boolean.FALSE;
                else {
                    try {
                        if (rawVal.contains(".")) {
                            value = Double.parseDouble(rawVal);
                        } else {
                            value = Long.parseLong(rawVal);
                        }
                    } catch (NumberFormatException e) {
                        value = rawVal;
                    }
                }
            }

            map.put(key, value);

            while (i < len && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ',')) i++;
        }
        return map;
    }

    private static List<Object> parseArrayInternal(String json) {
        List<Object> list = new ArrayList<>();
        int i = 1;
        int len = json.length() - 1;

        while (i < len) {
            while (i < len && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= len || json.charAt(i) == ']') break;

            char c = json.charAt(i);
            Object val = null;
            if (c == '"') {
                int start = ++i;
                while (i < len && (json.charAt(i) != '"' || json.charAt(i - 1) == '\\')) i++;
                val = unescape(json.substring(start, i));
                i++;
            } else if (c == '{') {
                int depth = 1;
                int start = i++;
                while (i < len && depth > 0) {
                    if (json.charAt(i) == '{') depth++;
                    else if (json.charAt(i) == '}') depth--;
                    i++;
                }
                val = parseObjectInternal(json.substring(start, i));
            } else if (c == '[') {
                int depth = 1;
                int start = i++;
                while (i < len && depth > 0) {
                    if (json.charAt(i) == '[') depth++;
                    else if (json.charAt(i) == ']') depth--;
                    i++;
                }
                val = parseArrayInternal(json.substring(start, i));
            } else {
                int start = i;
                while (i < len && json.charAt(i) != ',' && json.charAt(i) != ']') i++;
                String raw = json.substring(start, i).trim();
                if ("null".equals(raw)) val = null;
                else if ("true".equalsIgnoreCase(raw)) val = Boolean.TRUE;
                else if ("false".equalsIgnoreCase(raw)) val = Boolean.FALSE;
                else {
                    try {
                        if (raw.contains(".")) val = Double.parseDouble(raw);
                        else val = Long.parseLong(raw);
                    } catch (NumberFormatException e) {
                        val = raw;
                    }
                }
            }
            list.add(val);
            while (i < len && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ',')) i++;
        }
        return list;
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
