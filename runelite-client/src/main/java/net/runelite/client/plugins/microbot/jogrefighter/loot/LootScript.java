package net.runelite.client.plugins.microbot.jogrefighter.loot;

import net.runelite.api.ItemComposition;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.jogrefighter.JogreFighterConfig;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.ogPlugins.ogPrayer.enums.Bones;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.jogrefighter.combat.GlobalState.BonesCollected;

public class LootScript extends Script {

    private String[] lootItems;

    public LootScript() {

    }

    public void run(ItemSpawned itemSpawned) {
        mainScheduledFuture = scheduledExecutorService.schedule((() -> {
            if (!super.run()) return;
            if (Microbot.getClientThread().runOnClientThread(Rs2Inventory::isFull)) return;
            final ItemComposition itemComposition = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getItemDefinition(itemSpawned.getItem().getId()));
            for (String item : lootItems) {
                LocalPoint itemLocation = itemSpawned.getTile().getLocalLocation();
                int distance = itemSpawned.getTile().getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation());
                if (item.equalsIgnoreCase(itemComposition.getName()) && distance < 14) {
                    Rs2GroundItem.interact(item, "Take");
                    Microbot.pauseAllScripts = true;
                    sleepUntilOnClientThread(() -> Microbot.getClient().getLocalPlayer().getWorldLocation() == itemSpawned.getTile().getWorldLocation(), 4000);
                    sleep(2000, 3000);
                    Microbot.pauseAllScripts = false;
                    if (item.equalsIgnoreCase("Jogre bones")) {
                        BonesCollected += 1;
                        Microbot.status = "Bones collected: " + BonesCollected;
                    }
                }
            }
        }), 2000, TimeUnit.MILLISECONDS);
    }

    public boolean run(JogreFighterConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay((() -> {
            if (!super.run()) return;


            List<String> itemsToLoot = Arrays.asList("Jogre bones", "Nature rune", "Avantoe Seed",
                    "Grimy ranarr weed", "Grimy avantoe", "Grimy kwuarm", "Grimy cadantine",
                    "Ranarr seed", "Snapdragon seed", "Snape grass seed", "Cadantine seed",
                    "Torstol seed", "Shield left half", "Dragon spear", "Rune spear",
                    "Tooth half of key", "Loop half of key");


            //if (config.toggleLootArrows()) {
                /*for (String lootItem : Arrays.asList("bronze arrow", "iron arrow", "steel arrow", "mithril arrow", "adamant arrow", "rune arrow", "dragon arrow")) {
                    if (Rs2GroundItem.loot(lootItem, 13, 14))
                        break;
                }
            }*/
            /*if (!config.toggleLootItems()) return;
            for (String specialItem : Arrays.asList("Giant key")) {
                if (Rs2GroundItem.loot(specialItem, 13, 14))
                    break;
            }*/
            //boolean result = Rs2GroundItem.lootItemBasedOnValue(config.priceOfItemsToLoot(), 14);

            boolean itemLooted = false;

            // Loop through each item in the list and attempt to loot it
            for (String item : itemsToLoot) {
                if (Rs2GroundItem.loot(item, 1, 14)) {
                    itemLooted = true;
                    break; // Exit the loop if an item is successfully looted
                }
            }

            // If any item was looted, perform the following actions
            if (itemLooted) {
                Global.sleep(1000, 2000);
                Microbot.pauseAllScripts = false;
            }

        }), 0, 2000, TimeUnit.MILLISECONDS);
        return true;
    }

    public void shutdown() {
        super.shutdown();
        lootItems = null;
    }
}
