package solarsmp.TradeSystem;

import solarsmp.TradeSystem.Language.Language;
import solarsmp.TradeSystem.Language.Phrase;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TradeInventoryEventHandler implements Listener {

    public static void CancelAllTrades()
    {
        while(!Trades.isEmpty())
        {
            Trades.values().toArray(new Trade[0])[0].CancelTrade(null);
        }
    }

    public static HashMap<UUID, Trade> Trades = new HashMap<>();

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent e)
    {
        if(e.getClickedInventory() == null) return;
        if(!Trades.containsKey(e.getWhoClicked().getUniqueId())) return;

        UUID uuid = e.getWhoClicked().getUniqueId();
        Trade trade = Trades.get(uuid);
        int slot = e.getSlot();
        int row = slot / 9;
        int col = slot % 9;
        boolean HasConfirmed = trade.IsConfirmed(uuid);

        if(e.getClick() == ClickType.DOUBLE_CLICK)
        {

            ItemStack cursor = e.getCursor();
            assert cursor != null;
            if(e.getCursor().getType().isAir()) return;

            if(!HasConfirmed) cursor = trade.DoubleClickFillup(cursor, uuid);

            Inventory inv = e.getWhoClicked().getInventory();
            for(int i = 9; i < 36; ++i) {

                if (cursor.getMaxStackSize() == 1 || cursor.getAmount() == cursor.getMaxStackSize()) break;

                if(i % 9 > 3) i += 5;
                ItemStack slotItem = inv.getItem(i);
                if(slotItem == null || slotItem.getType() == Material.AIR) continue;
                if(!slotItem.getType().equals(cursor.getType())) continue;
                if(!Objects.equals(slotItem.getItemMeta(), cursor.getItemMeta())) continue;

                int subtract = Math.min(cursor.getMaxStackSize() - cursor.getAmount(), slotItem.getAmount());
                slotItem.setAmount(slotItem.getAmount() - subtract);
                cursor.setAmount(cursor.getAmount() + subtract);
            }

            e.setCancelled(true);
            return;
        }

        if(e.isShiftClick())
        {
            if(HasConfirmed)
            {
                e.setCancelled(true);
                return;
            }

            if(e.getClickedInventory().equals(e.getInventory()))
            {
                if(slot % 9 <= 3 && row > 0 && row < 4)
                {
                    trade.SyncSlotFor(uuid, e.getSlot());
                }
                else
                {
                    e.setCancelled(true);
                }
                return;
            }


            e.setCancelled(true);
            ItemStack result = trade.ShiftClickInsert(e.getCurrentItem(), uuid);
            e.setCurrentItem(result);
            return;
        }


        assert e.getClickedInventory() != null;
        if(!e.getClickedInventory().equals(e.getInventory())) return;


        if(col > 3 || row == 0 || row >= 4 || HasConfirmed)
        {

            e.setCancelled(true);

            if(row == 4 && col <= 3)
            {
                trade.ToggleTradeConfirm(uuid);
            }
            return;
        }

        trade.SyncSlotFor(uuid, slot);
    }

    public static Set<Integer> IllegalSlots = new HashSet<>();

    @EventHandler
    public void OnInventoryDrag(InventoryDragEvent e)
    {
        if(!Trades.containsKey(e.getWhoClicked().getUniqueId())) return;
        e.getRawSlots();
        for(int slot : IllegalSlots)
        {
            if(!e.getRawSlots().contains(slot)) continue;
            e.setCancelled(true);

            break;
        }

        UUID uuid = e.getWhoClicked().getUniqueId();
        Trade trade = Trades.get(uuid);
        if(trade.IsConfirmed(uuid))
        {
            e.setCancelled(true);
            return;
        }

        for(int slot : e.getRawSlots())
        {
            int row = slot / 9;
            int col = slot % 9;
            if(col > 3 || row == 0 || row >= 4) continue;
            trade.SyncSlotFor(e.getWhoClicked().getUniqueId(), slot);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e)
    {

        if(Trades.containsKey(e.getPlayer().getUniqueId()))
        {
            Trades.get(e.getPlayer().getUniqueId()).CancelTrade(e.getPlayer().getUniqueId());
            Runtime.getRuntime().gc();
        }
        else if(e.getView().getTitle().startsWith(Language.get(e.getPlayer().getUniqueId(), Phrase.TRADE_INVENTORY_CONCLUSION_TITLE).replace("%name%", "")))
        {
            Player p = (Player) e.getPlayer();
            if(e.getInventory().isEmpty()) return;
            List<ItemStack> LeftOvers = new ArrayList<>();
            for(final ItemStack i : e.getInventory().getContents()) {
                if(i == null) continue;
                LeftOvers.addAll(p.getInventory().addItem(i).values());
            }
            for(final ItemStack drops : LeftOvers)
            {
                p.getWorld().dropItemNaturally(p.getLocation(), drops);
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e)
    {
        UUID uuid = e.getPlayer().getUniqueId();

        TradeCommand.clearPendingTrades(uuid);
    }

    @EventHandler
    public void onGameModeChanged(PlayerGameModeChangeEvent e)
    {
        UUID uuid = e.getPlayer().getUniqueId();
        if(!e.getNewGameMode().equals(GameMode.SPECTATOR)) return;
        TradeCommand.clearPendingTrades(uuid);
        if(Trades.containsKey(uuid)) e.getPlayer().closeInventory();
    }

}
