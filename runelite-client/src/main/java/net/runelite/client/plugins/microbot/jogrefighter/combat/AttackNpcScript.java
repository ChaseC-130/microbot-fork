package net.runelite.client.plugins.microbot.jogrefighter.combat;

import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.jogrefighter.JogreFighterConfig;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AttackNpcScript extends Script {

    String[] configAttackableNpcs;

    public static Actor currentNpc = null;

    public static List<NPC> attackableNpcs = new ArrayList();

    boolean clicked = false;

    public void run(JogreFighterConfig config) {
        //String npcToAttack = Arrays.stream(Arrays.stream(config.attackableNpcs().split(",")).map(String::trim).toArray(String[]::new)).findFirst().get();
        String npcToAttack = "Jogre";

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run() || GlobalState.status != GlobalState.Status.READY_FOR_COMBAT) return;

                if (Rs2Inventory.isFull() || ((double)(Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) * 100) / Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS) < 25)) {
                    System.out.println("Going to bank...");
                    GlobalState.status = GlobalState.Status.READY_FOR_BANK;
                    return;
                }
                 attackableNpcs =  Microbot.getClient().getNpcs().stream()
                        .sorted(Comparator.comparingInt(value -> value.getLocalLocation().distanceTo(Microbot.getClient().getLocalPlayer().getLocalLocation())))
                        .filter(x -> !x.isDead()
                                && x.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation()) < 12
                                && (!x.isInteracting() || x.getInteracting() == Microbot.getClient().getLocalPlayer())
                                && (x.getInteracting() == null  || x.getInteracting() == Microbot.getClient().getLocalPlayer())
                                && x.getAnimation() == -1 && x.getName().equalsIgnoreCase(npcToAttack)).collect(Collectors.toList());
                Player player = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getLocalPlayer());
                if (Rs2Combat.inCombat()) {
                    return;
                }

                for (NPC npc : attackableNpcs) {
                    if (npc == null
                            || npc.getAnimation() != -1
                            || npc.isDead()
                            || (npc.getInteracting() != null && npc.getInteracting() != Microbot.getClient().getLocalPlayer())
                            || (npc.isInteracting()  && npc.getInteracting() != Microbot.getClient().getLocalPlayer())
                            || !npc.getName().equalsIgnoreCase(npcToAttack))
                        break;
                    if (npc.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation()) > 12)
                        break;
                    if (!Rs2Camera.isTileOnScreen(npc.getLocalLocation()))
                        Rs2Camera.turnTo(npc);

                    if (!Microbot.getWalker().canReach(npc.getWorldLocation()))
                        continue;
                    Rs2Npc.interact(npc, "attack");
                    sleepUntil(() -> Microbot.getClient().getLocalPlayer().isInteracting() && Microbot.getClient().getLocalPlayer().getInteracting() instanceof NPC);
                    break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    public static void skipNpc() {
        currentNpc = null;
    }

    public void shutdown() {
        super.shutdown();
        configAttackableNpcs = null;
        clicked = false;
    }
}