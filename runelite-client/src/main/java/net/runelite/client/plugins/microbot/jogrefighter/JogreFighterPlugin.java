package net.runelite.client.plugins.microbot.jogrefighter;

import com.google.inject.Provides;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.jogrefighter.combat.*;
import net.runelite.client.plugins.microbot.jogrefighter.loot.LootScript;
import net.runelite.client.plugins.microbot.jogrefighter.navigate.BankRunScript;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@PluginDescriptor(
        name = "<html>[<font color=yellow>BIGJ</font>] " + "JogreFighter",
        description = "BigJohnson Jogrefighter plugin",
        tags = {"jogre", "bigj", "misc", "combat"},
        enabledByDefault = false
)
public class JogreFighterPlugin extends Plugin {
    @Inject
    private JogreFighterConfig config;

    @Provides
    JogreFighterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JogreFighterConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private JogreFighterOverlay jogreFighterOverlay;


    private final AttackNpcScript attackNpc = new AttackNpcScript();

    private final BankRunScript bankScript = new BankRunScript();
    private final FoodScript foodScript = new FoodScript();

    private final LootScript lootScript = new LootScript();

    private final FlickerScript flickerScript = new FlickerScript();
    private final UseSpecialAttackScript useSpecialAttackScript = new UseSpecialAttackScript();



    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    @Override
    protected void startUp() throws AWTException {
        Microbot.pauseAllScripts = false;
        if (overlayManager != null) {
            overlayManager.add(jogreFighterOverlay);
        }
        lootScript.run(config);
        attackNpc.run(config);
        foodScript.run(config);
        bankScript.run(config);
        useSpecialAttackScript.run(config);
    }

    protected void shutDown() {
        lootScript.shutdown();
        attackNpc.shutdown();
        foodScript.shutdown();
        bankScript.shutdown();
        useSpecialAttackScript.shutdown();
        overlayManager.remove(jogreFighterOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getMessage().contains("reach that")) {
            AttackNpcScript.skipNpc();
        }
    }

}
