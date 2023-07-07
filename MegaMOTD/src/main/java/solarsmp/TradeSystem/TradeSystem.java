package solarsmp.TradeSystem;

import org.bukkit.plugin.java.JavaPlugin;

import solarsmp.TradeSystem.Language.Language;
import solarsmp.TradeSystem.Language.LanguageInventory;
import solarsmp.TradeSystem.Language.Phrase;
import solarsmp.TradeSystem.other.TradeSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class TradeSystem extends JavaPlugin {


    public static TradeSystem main;



    @Override
    public void onEnable() {
        // Plugin startup logic
        main = this;
        Init();
        Language.Init();
        // trade command, setting page with privacy and 2fa/double check like in Rocket League
        Bukkit.getPluginManager().registerEvents(new TradeInventoryEventHandler(), this);
        Bukkit.getPluginManager().registerEvents(new TradeSettings(), this);
        Bukkit.getPluginManager().registerEvents(new LanguageInventory(), this);
        this.getCommand("trade").setExecutor(new TradeCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        TradeInventoryEventHandler.CancelAllTrades();
    }

    public static ItemStack getConfirmRedOwn(Player player) { return getConfirmRedOwn(player == null ? null : player.getUniqueId()); }
    public static ItemStack getConfirmRedOwn(UUID uuid)
    {
        ItemStack it = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta m = it.getItemMeta();
        assert m != null;
        m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_CONFIRM_TRADE));
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack getConfirmRedOther(Player player) { return getConfirmRedOther(player == null ? null : player.getUniqueId()); }
    public static ItemStack getConfirmRedOther(UUID uuid)
    {
        ItemStack it = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta m = it.getItemMeta();
        assert m != null;
        m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_PARTNER_NOT_CONFIRMED));
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack getConfirmGreenOwn(Player player) { return getConfirmGreenOwn(player == null ? null : player.getUniqueId()); }
    public static ItemStack getConfirmGreenOwn(UUID uuid)
    {
        ItemStack it = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
        ItemMeta m = it.getItemMeta();
        assert m != null;
        m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_CONFIRM_TRADE));
        m.setLore(Collections.singletonList(Language.get(uuid, Phrase.ITEM_LORE_CLICK_TO_RESCIND)));
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack getConfirmGreenOther(Player player) { return getConfirmGreenOther(player == null ? null : player.getUniqueId()); }
    public static ItemStack getConfirmGreenOther(UUID uuid)
    {
        ItemStack it = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
        ItemMeta m = it.getItemMeta();
        assert m != null;
        m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_PARTNER_CONFIRMED));
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack getConfirmLoadingBar(Player player) { return getConfirmLoadingBar(player == null ? null : player.getUniqueId()); }
    public static ItemStack getConfirmLoadingBar(UUID uuid)
    {
        ItemStack it = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
        ItemMeta m = it.getItemMeta();
        assert m != null;
        m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_PROCESSING_TRADE));
        it.setItemMeta(m);
        return it;
    }

    public void Init()
    {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta m = it.getItemMeta();
        assert m != null;
        m.setDisplayName("§f ");
        it.setItemMeta(m);
        Trade.EmptyStack = it;

        for(int i = 0; i < 54; ++i)
        {
            int col = i % 9, row = i / 9;
            if(row > 0 && row < 4 && col <= 3) continue;
            TradeInventoryEventHandler.IllegalSlots.add(i);
        }
    }
}
