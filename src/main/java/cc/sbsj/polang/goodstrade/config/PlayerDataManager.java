package cc.sbsj.polang.goodstrade.config;

import cc.sbsj.polang.goodstrade.GoodsTrade;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家数据管理器
 */
public class PlayerDataManager {

    private static final String FILE_NAME = "PlayerData.yml";
    private static final String TRADE_ACCEPT_KEY = "TradeAccept";

    private final File file;
    private FileConfiguration data;

    // 内存缓存：UUID -> 是否接受交易请求（默认 true）
    private final Map<UUID, Boolean> tradeAcceptCache = new ConcurrentHashMap<>();

    public PlayerDataManager(GoodsTrade plugin) {
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        load();
    }

    /**
     * 启动时从文件加载玩家数据到缓存
     */
    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                GoodsTrade.instance.getLogger().warning("创建 PlayerData.yml 失败: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);

        // 加载交易接受状态到缓存
        if (data.isConfigurationSection(TRADE_ACCEPT_KEY)) {
            for (String uuidStr : data.getConfigurationSection(TRADE_ACCEPT_KEY).getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    boolean accept = data.getBoolean(TRADE_ACCEPT_KEY + "." + uuidStr, true);
                    tradeAcceptCache.put(uuid, accept);
                } catch (IllegalArgumentException e) {
                    // 忽略无效的 UUID
                }
            }
        }
    }

    /**
     * 服务器关闭时将缓存写回文件
     */
    public void save() {
        // 重建配置，只保留缓存中的数据
        data = new YamlConfiguration();
        for (Map.Entry<UUID, Boolean> entry : tradeAcceptCache.entrySet()) {
            data.set(TRADE_ACCEPT_KEY + "." + entry.getKey().toString(), entry.getValue());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            GoodsTrade.instance.getLogger().warning("保存 PlayerData.yml 失败: " + e.getMessage());
        }
    }

    /**
     * 获取玩家是否接受交易请求（默认 true）
     */
    public boolean isTradeAccept(UUID uuid) {
        return tradeAcceptCache.getOrDefault(uuid, true);
    }

    /**
     * 设置玩家是否接受交易请求（仅修改缓存）
     */
    public void setTradeAccept(UUID uuid, boolean accept) {
        tradeAcceptCache.put(uuid, accept);
    }

    /**
     * 切换玩家交易接受状态，返回切换后的状态
     */
    public boolean toggleTradeAccept(UUID uuid) {
        boolean newState = !isTradeAccept(uuid);
        tradeAcceptCache.put(uuid, newState);
        return newState;
    }
}
