package cc.sbsj.polang.goodstrade.config;

import cc.sbsj.polang.goodstrade.GoodsTrade;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Lang {
    private static final String LANG_FILE = "Lang.yml";
    private YamlConfiguration langConfig;
    private final GoodsTrade plugin;

    public Lang(GoodsTrade plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File langFile = new File(plugin.getDataFolder(), LANG_FILE);
        if (!langFile.exists()) {
            plugin.saveResource(LANG_FILE, false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getString(String path) {
        String value = langConfig.getString(path);
        if (value == null) {
            plugin.getLogger().warning("Lang.yml 缺少配置项: " + path);
            return "";
        }
        return color(value);
    }

    public String getString(String path, String defaultValue) {
        String value = langConfig.getString(path, defaultValue);
        return color(value);
    }

    public List<String> getStringList(String path) {
        List<String> values = langConfig.getStringList(path);
        if (values.isEmpty()) {
            // 尝试读取单个字符串
            String single = langConfig.getString(path);
            if (single != null) {
                List<String> result = new ArrayList<>();
                result.add(color(single));
                return result;
            }
            plugin.getLogger().warning("Lang.yml 缺少配置项: " + path);
            return new ArrayList<>();
        }
        List<String> colored = new ArrayList<>();
        for (String value : values) {
            colored.add(color(value));
        }
        return colored;
    }

    public String replacePlaceholders(String text, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            plugin.getLogger().warning("占位符参数数量必须是偶数");
            return text;
        }
        for (int i = 0; i < placeholders.length; i += 2) {
            text = text.replace(placeholders[i], placeholders[i + 1]);
        }
        return text;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
