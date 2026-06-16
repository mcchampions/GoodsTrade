package cc.sbsj.polang.goodstrade.util;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class NbtBlackList {
    private static final List<Class<?>> VALUE_TYPES = Arrays.asList(
            String.class,
            Integer.class,
            Long.class,
            Double.class,
            Float.class,
            Short.class,
            Byte.class,
            Boolean.class,
            int[].class,
            long[].class,
            byte[].class
    );

    private final List<Rule> rules;

    private NbtBlackList(List<Rule> rules) {
        this.rules = rules;
    }

    public static NbtBlackList fromConfig(List<String> configRules) {
        if (configRules == null || configRules.isEmpty()) {
            return new NbtBlackList(Collections.emptyList());
        }

        List<Rule> rules = new ArrayList<>();
        for (String configRule : configRules) {
            Rule rule = Rule.parse(configRule);
            if (rule != null) {
                rules.add(rule);
            }
        }
        return new NbtBlackList(rules);
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }

    public boolean contains(ItemStack item) {
        if (isEmpty() || !Utils.isItemStackNotEmpty(item)) return false;
        try {
            return NBT.get(item, this::matches);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean matches(ReadableNBT nbt) {
        for (Rule rule : rules) {
            if (rule.matches(nbt)) {
                return true;
            }
        }
        return false;
    }

    private static class Rule {
        private final String path;
        private final String value;

        private Rule(String path, String value) {
            this.path = path;
            this.value = value;
        }

        private static Rule parse(String rule) {
            if (rule == null) return null;
            String trimmed = rule.trim();
            if (trimmed.isEmpty()) return null;
            int splitIndex = trimmed.indexOf('@');
            if (splitIndex < 0) {
                return new Rule(trimmed, null);
            }
            String path = trimmed.substring(0, splitIndex).trim();
            String value = trimmed.substring(splitIndex + 1).trim();
            if (path.isEmpty()) return null;
            return new Rule(path, value);
        }

        private boolean matches(ReadableNBT nbt) {
            Object nbtValue = resolveValue(nbt, path);
            if (value == null) {
                return nbtValue != null || nbt.resolveCompound(path) != null || hasTag(nbt, path);
            }
            if (nbtValue == null) {
                ReadableNBT compound = nbt.resolveCompound(path);
                return compound != null && compound.toString().equals(value);
            }
            return stringify(nbtValue).equals(stripQuotes(value));
        }

        private static Object resolveValue(ReadableNBT nbt, String path) {
            for (Class<?> type : VALUE_TYPES) {
                Object value = resolveOrNull(nbt, path, type);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        private static Object resolveOrNull(ReadableNBT nbt, String path, Class<?> type) {
            if (type == String.class) return nbt.resolveOrNull(path, String.class);
            if (type == Integer.class) return nbt.resolveOrNull(path, Integer.class);
            if (type == Long.class) return nbt.resolveOrNull(path, Long.class);
            if (type == Double.class) return nbt.resolveOrNull(path, Double.class);
            if (type == Float.class) return nbt.resolveOrNull(path, Float.class);
            if (type == Short.class) return nbt.resolveOrNull(path, Short.class);
            if (type == Byte.class) return nbt.resolveOrNull(path, Byte.class);
            if (type == Boolean.class) return nbt.resolveOrNull(path, Boolean.class);
            if (type == int[].class) return nbt.resolveOrNull(path, int[].class);
            if (type == long[].class) return nbt.resolveOrNull(path, long[].class);
            if (type == byte[].class) return nbt.resolveOrNull(path, byte[].class);
            return null;
        }

        private static boolean hasTag(ReadableNBT nbt, String path) {
            PathParts parts = PathParts.of(path);
            ReadableNBT compound = parts.parent == null ? nbt : nbt.resolveCompound(parts.parent);
            return compound != null && compound.hasTag(parts.key);
        }

        private static String stringify(Object value) {
            if (value instanceof int[]) return Arrays.toString((int[]) value);
            if (value instanceof long[]) return Arrays.toString((long[]) value);
            if (value instanceof byte[]) return Arrays.toString((byte[]) value);
            return stripQuotes(String.valueOf(value));
        }

        private static String stripQuotes(String value) {
            if (value == null || value.length() < 2) return value;
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
    }

    private static class PathParts {
        private final String parent;
        private final String key;

        private PathParts(String parent, String key) {
            this.parent = parent;
            this.key = key;
        }

        private static PathParts of(String path) {
            int lastDot = -1;
            boolean escaped = false;
            for (int i = 0; i < path.length(); i++) {
                char c = path.charAt(i);
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    continue;
                }
                if (c == '.') {
                    lastDot = i;
                }
            }
            if (lastDot < 0) {
                return new PathParts(null, unescape(path));
            }
            return new PathParts(path.substring(0, lastDot), unescape(path.substring(lastDot + 1)));
        }

        private static String unescape(String value) {
            return value.replace("\\.", ".");
        }
    }
}
