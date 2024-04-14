package net.runelite.client.plugins.microbot.jogrefighter.navigate;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.jogrefighter.JogreFighterConfig;
import net.runelite.client.plugins.microbot.Script;


import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.runelite.client.plugins.microbot.jogrefighter.JogreFighterPlugin;
import net.runelite.client.plugins.microbot.jogrefighter.combat.GlobalState;
import net.runelite.client.plugins.microbot.jogrefighter.enums.Status;
import net.runelite.client.plugins.microbot.staticwalker.StaticWalkerScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;

import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;

import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;


public class BankRunScript extends Script {
    long statusChangeTime = System.currentTimeMillis(); // Timestamp when the status last changed

    public Status status = Status.STARTUP;

    private final WorldPoint VARROCK_WEST_BANK = new WorldPoint(3183, 3441, 0);
    private final WorldPoint PORT_SARIM_DOCKS = new WorldPoint(3028, 3218, 0);
    private final WorldPoint KARAMAJA_FENCE = new WorldPoint(2816, 3182, 0);
    private final WorldPoint POTHOLE_DUNGEON_1 = new WorldPoint(2821, 3122, 0);
    private final WorldPoint POTHOLE_DUNGEON_3 = new WorldPoint(2862, 3117, 0);
    private final WorldPoint POTHOLE_DUNGEON_5 = new WorldPoint(2761, 3170, 0);
    private final WorldPoint POTHOLE_DUNGEON_6 = new WorldPoint(2803, 3183, 0);
    private final WorldPoint POTHOLE_DUNGEON_4 = new WorldPoint(2808, 3115, 0);
    private final WorldPoint POTHOLE_DUNGEON_2 = new WorldPoint(2833, 3132, 0);
    private int dungeonNumber = 6;

    private boolean stateChange = true;

    private final int PROXIMITY_THRESHOLD = 5;

    public void run(JogreFighterConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                if (System.currentTimeMillis() - statusChangeTime > TimeUnit.MINUTES.toMillis(1)) {
                    stateChange = true;
                    status = Status.STARTUP;
                    statusChangeTime = System.currentTimeMillis();
                }
                if (GlobalState.status == GlobalState.Status.READY_FOR_BANK) {
                    status = Status.READY_FOR_BANK;
                    GlobalState.status = GlobalState.Status.OTHER;
                }
                switch (status) {
                    case STARTUP:
                        if (Rs2Inventory.hasItem("Varrock Teleport") && Rs2Inventory.hasItem("Teleport to House") && (Rs2Inventory.hasItem("Shark") || Rs2Inventory.hasItem("Monkfish"))) {
                            status = Status.TELEPORT_TO_HOUSE;
                            GlobalState.status = GlobalState.Status.OTHER;
                            Microbot.status = "Heading to Jogres";
                        }
                        if (Rs2Inventory.hasItem("Varrock Teleport") && !Rs2Inventory.hasItem("Shark") && !Rs2Inventory.hasItem("Monkfish")) {
                            status = Status.READY_FOR_BANK;
                            GlobalState.status = GlobalState.Status.READY_FOR_BANK;
                        }
                        if (Rs2Inventory.hasItem("Varrock Teleport") && (Rs2Inventory.hasItem("Shark") || Rs2Inventory.hasItem("Monkfish"))) {
                            int playerY = getPlayerLocation().getY();
                            int playerX = getPlayerLocation().getX();
                            System.out.println("playery: " + playerY);
                            System.out.println("playerX: " + playerX);
                            if (playerY >= 3135 && playerY < 3205 && playerX >= 2816 && playerX < 2960) {
                                System.out.println("Navigating to Fence");
                                status = Status.NAVIGATE_TO_FENCE;
                                dungeonNumber = 6;
                                GlobalState.status = GlobalState.Status.OTHER;
                            } else if (playerX <= 2870 && playerX > 2720 && playerY < 3138 && playerY > 3092) {
                                System.out.println("Navigating to Dungeon");
                                status = Status.NAVIGATE_TO_DUNGEON;
                                GlobalState.status = GlobalState.Status.OTHER;
                            } else if (playerX <= 2815 && playerX > 2720 && playerY < 3219 && playerY > 3092) {
                                System.out.println("Navigating to Dungeon");
                                status = Status.NAVIGATE_TO_DUNGEON;
                                GlobalState.status = GlobalState.Status.OTHER;
                            } else if (playerY > 3194 && playerY < 3250 && playerX > 2937 && playerX < 3050) {
                                System.out.println("Navigating to Port Sarim");
                                status = Status.TRAVEL_TO_PORT_SARIM;
                                GlobalState.status = GlobalState.Status.OTHER;
                            } else if (playerY <= 9532 && playerY > 9475 && playerX > 2820 && playerX < 2875) {
                                System.out.println("Ready for combat");
                                status = Status.READY_FOR_COMBAT;
                                GlobalState.status = GlobalState.Status.READY_FOR_COMBAT;
                            } else {
                                status = Status.READY_FOR_BANK;
                                GlobalState.status = GlobalState.Status.OTHER;
                            }
                        }
                        if (!Rs2Inventory.hasItem("Varrock Teleport")) {
                            if (isInVarrock()) {
                                status = Status.BANKING;
                                GlobalState.status = GlobalState.Status.OTHER;
                            } else {
                             Microbot.status = "Please start the script in Varrock or with a Varrock teleport.";
                             Rs2Player.logout();
                            }
                        }
                        statusChangeTime = System.currentTimeMillis();
                        break;
                    case READY_FOR_BANK:
                        if (stateChange) {
                            stateChange = false;
                            statusChangeTime = System.currentTimeMillis();
                        }
                        useTeleportToVarrock();
                        break;
                    case BANKING:
                        if (stateChange) {
                            stateChange = false;
                            statusChangeTime = System.currentTimeMillis();
                        }
                        if (!isCloseToDestination(VARROCK_WEST_BANK)) {
                            navigateToVarrockWestBank();
                        } else if (waitForArrival(VARROCK_WEST_BANK, 60000)) {
                            interactWithBanker();
                            bankItemsAndWithdrawSharks();
                        } else {
                            Rs2Player.logout();
                        }
                        break;
                    case TELEPORT_TO_HOUSE:
                        if (stateChange) {
                            stateChange = false;
                            statusChangeTime = System.currentTimeMillis();
                        }
                        useTeleportToHouse();
                        break;
                    case TRAVEL_TO_PORT_SARIM:
                        if (stateChange) {
                            stateChange = false;
                            statusChangeTime = System.currentTimeMillis();
                        }
                        if (!isCloseToDestination(PORT_SARIM_DOCKS)) {
                            navigateToPortSarim();
                        } else if (waitForArrival(PORT_SARIM_DOCKS, 60000)) {
                            interactWithSeaman();
                        } else {
                            Rs2Player.logout();

                        }
                        break;
                    case NAVIGATE_TO_FENCE:
                        System.out.println("Status is navigate to fence");
                        if (stateChange) {
                            stateChange = false;
                            statusChangeTime = System.currentTimeMillis();
                        }
                        if (isCloseToDestination(KARAMAJA_FENCE)) {
                            interactWithFence();
                        } else {
                            navigateToFence();
                        }
                        break;
                    case NAVIGATE_TO_DUNGEON:
                        System.out.println("Status is navigate to dungeon");
                        if (stateChange) {
                            stateChange = false;
                            statusChangeTime = System.currentTimeMillis();
                        }
                        if (isCloseToDestination(POTHOLE_DUNGEON_1)) {
                            interactWithHole();
                        } else {
                            navigateToPotholeDungeon();
                        }
                        break;
                    case READY_FOR_COMBAT:
                        if (stateChange) {
                            stateChange = false;
                            statusChangeTime = System.currentTimeMillis();
                        }
                        break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    private void navigateToVarrockWestBank() {
        Microbot.status = "Heading to bank";

        WorldPoint playerLocation = getPlayerLocation(); // Implement getPlayerLocation based on your bot framework

        if (playerLocation.distanceTo(VARROCK_WEST_BANK) > 1) {
            Microbot.getWalker().hybridWalkTo(VARROCK_WEST_BANK, true);
            sleep(500, 4000);
        }

    }

    private boolean isCloseToDestination(WorldPoint destination) {
        WorldPoint playerLocation = getPlayerLocation(); // Implement getPlayerLocation based on your bot framework
        return playerLocation.distanceTo(destination) <= PROXIMITY_THRESHOLD;
    }

    private boolean isInVarrock() {
        WorldPoint playerLocation = getPlayerLocation(); // Implement getPlayerLocation based on your bot framework
        return playerLocation.distanceTo(VARROCK_WEST_BANK) <= 400;
    }



    private boolean waitForArrival(WorldPoint destination, long timeout) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            if (isCloseToDestination(destination)) {
                //walkerScript.shutdown();
                return true;
            }
            sleep(1000);  // Sleep for a bit before re-checking
        }
        return false;
    }

    private void interactWithBanker() {
        Microbot.status = "Banking";

        List<NPC> bankers = Microbot.getClient().getNpcs().stream()
                .filter(npc -> npc.getName().contains("Banker") && npc.getWorldLocation().distanceTo(getPlayerLocation()) < 5)
                .collect(Collectors.toList());
        if (!bankers.isEmpty()) {
            Rs2Npc.interact(bankers.get(0), "Bank"); // Assuming Rs2Npc.interact can be used like this
            sleepUntil(Rs2Bank::isOpen, 5000); // Wait until the bank interface is open
        }
    }

    private void interactWithSeaman() {
        Microbot.status = "Taking ship to Karamja";

        List<NPC> seaman = Microbot.getClient().getNpcs().stream()
                //.filter(npc -> (npc.getName().contains("Seaman") || npc.getName().contains("Captain Tobias")))
                .filter(npc -> (npc.getName().contains("Captain Tobias")))
                .collect(Collectors.toList());

        if (!seaman.isEmpty()) {
            Rs2Npc.interact(seaman.get(0), "Pay-fare");
            //Rs2Npc.interact(seaman.get(0));
            sleepUntil(() -> Rs2Widget.hasWidget("click here to continue"));
            Rs2Widget.clickWidget("click here to continue");

            sleepUntil(() -> Rs2Widget.hasWidget("Select an Option"));
            Rs2Widget.clickWidget("Yes please.");

            sleepUntil(() -> Rs2Widget.hasWidget("click here to continue"));
            Rs2Widget.clickWidget("click here to continue");


            sleep(9000, 12000);
            WorldPoint playerLocation = getPlayerLocation();

            if (playerLocation.distanceTo(PORT_SARIM_DOCKS) > 50) {
                sleepUntil(() -> Rs2GameObject.findObject("Gangplank") != null);
                Rs2GameObject.interact("Gangplank", "Cross");

                sleep(2000, 3000);

                status = Status.NAVIGATE_TO_FENCE;
            }

        }
    }

    private void interactWithFence() {

        TileObject closedGate = Rs2GameObject.findDoor(1568);
        WorldPoint playerLocation = getPlayerLocation();

        if (closedGate != null) {
            Microbot.status = "Opening gate";
            System.out.println("Interacting with 1568");
            Rs2GameObject.interact(closedGate);
            sleepUntil(() -> playerLocation.distanceTo(KARAMAJA_FENCE) < 2, 3000);
            sleep(100);
            Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON_6, true);
            status = Status.NAVIGATE_TO_DUNGEON;
        } else {
            closedGate = Rs2GameObject.findDoor(1727);
            if (closedGate != null) {
                System.out.println("Interacting with 1727");
                Microbot.status = "Opening gate";
                Rs2GameObject.interact(closedGate);
                sleepUntil(() -> playerLocation.distanceTo(KARAMAJA_FENCE) < 2, 3000);
                sleep(100);
                Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON_6, true);
                status = Status.NAVIGATE_TO_DUNGEON;
            }
        }

        sleepUntil(() -> playerLocation.distanceTo(KARAMAJA_FENCE) < 2, 3000);
        Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON_6, true);
        Microbot.status ="Gate is open";
        status = Status.NAVIGATE_TO_DUNGEON;


    }

    private void interactWithHole() {
        GameObject rocks = Rs2GameObject.findObject("Rocks");
        if (rocks != null) {
            if (Rs2GameObject.hasAction(rocks, "Search")) {
                Rs2GameObject.interact(rocks, "Search");
                sleepUntil(() -> Rs2Widget.hasWidget("click here to continue"), 3500);
                sleep(500, 1000);
                Rs2Widget.clickWidget("click here to continue");

                sleepUntil(() -> Rs2Widget.hasWidget("Would you like to enter the cave?"), 2000);
                sleep(500, 1000);
                Rs2Widget.clickWidget("Yes, I'll enter the cave.");

                sleepUntil(() -> Rs2Widget.hasWidget("click here to continue"), 2000);
                sleep(500, 1000);
                Rs2Widget.clickWidget("click here to continue");
                sleep(500, 1000);

                GameObject nearbyRocks = Rs2GameObject.findObject("Rocks");
                if (nearbyRocks != null) {
                    System.out.println("Rocks still found.");
                } else {
                    status = Status.READY_FOR_COMBAT;
                    Microbot.status = "Ready for combat";
                    GlobalState.status = GlobalState.Status.READY_FOR_COMBAT;
                }

            } else {
                dungeonNumber = 5;
            }
        }


    }
    private void bankItemsAndWithdrawSharks() {
        if (Rs2Bank.openBank()) {
            Rs2Bank.depositAll();
            sleep(1000);

            Rs2Bank.withdrawX("Coins", 30);
            sleep(300);

            int fish = ((225 - (Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS) + Microbot.getClient().getRealSkillLevel(Skill.DEFENCE))) / 15);

            if (Rs2Bank.hasBankItem("Monkfish")) {
                Rs2Bank.withdrawX("Monkfish", fish);
            } else {
                fish = (int)Math.ceil(fish * 0.65);
                Rs2Bank.withdrawX("Shark", fish);
            }
            sleep(300);
            Rs2Bank.withdrawItem("Teleport To House");
            sleep(300);
            Rs2Bank.withdrawItem("Varrock Teleport");
            sleep(300);

            Rs2Bank.closeBank();
            status = Status.TELEPORT_TO_HOUSE;
        }
    }



    private void useTeleportToHouse() {
        if (Rs2Inventory.hasItem("Teleport to house")) {
            Rs2Inventory.interact("Teleport to house", "break");
            sleep(3500, 6000);
            status = Status.TRAVEL_TO_PORT_SARIM;
        } else {
            Rs2Player.logout();

        }
    }

    private void useTeleportToVarrock() {
        if (Rs2Inventory.hasItem("Varrock teleport")) {
            Rs2Inventory.interact("Varrock teleport", "break");
            sleep(3500, 6000);
            status = Status.BANKING;
        }
    }

    private void navigateToPortSarim() {
        WorldPoint playerLocation = getPlayerLocation(); // Implement getPlayerLocation based on your bot framework

        if (playerLocation.distanceTo(PORT_SARIM_DOCKS) > 1) {
            Microbot.getWalker().hybridWalkTo(PORT_SARIM_DOCKS, true);
            sleep(500, 4000);
        }

        //status = Status.TRAVEL_TO_KARAMJA;
    }

    private void navigateToFence() {

        WorldPoint playerLocation = getPlayerLocation(); // Implement getPlayerLocation based on your bot framework

        if (playerLocation.distanceTo(KARAMAJA_FENCE) > 1) {
            Microbot.getWalker().hybridWalkTo(KARAMAJA_FENCE, true);
            sleep(500, 4000);
        } else {
            interactWithFence();
        }
    }


    private void navigateToPotholeDungeon() {
        //if (!navigationStarted) {
            //Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON, true);
            //navigationStarted = true;
        //}
        if (!isCloseToDestination(POTHOLE_DUNGEON_6) && !isCloseToDestination(POTHOLE_DUNGEON_5) && !isCloseToDestination(POTHOLE_DUNGEON_4) && !isCloseToDestination(POTHOLE_DUNGEON_3) && !isCloseToDestination(POTHOLE_DUNGEON_2) && !isCloseToDestination(POTHOLE_DUNGEON_1) && dungeonNumber == 6) {
            Microbot.status = "Navigating to Pothole dungeon";
            Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON_6, true);
            sleep(2500, 5000);
        }


        if (isCloseToDestination(POTHOLE_DUNGEON_6)) {
            dungeonNumber = 5;
        }

        if (dungeonNumber == 5) {
            Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON_5, true);
            sleep(2500, 5000);
        }

        if (isCloseToDestination(POTHOLE_DUNGEON_5)) {
            dungeonNumber = 4;
        }

        if (dungeonNumber == 4) {
            Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON_4, true);
            sleep(2500, 5000);
        }

        if (isCloseToDestination(POTHOLE_DUNGEON_4)) {
            dungeonNumber = 3;


        }

        if (dungeonNumber == 3) {
            Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON_3, true);
            sleep(2500, 5000);
        }

        if (isCloseToDestination(POTHOLE_DUNGEON_3)) {


            dungeonNumber = 2;
        }

        if (dungeonNumber == 2) {
            Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON_2, true);
            sleep(2500, 5000);
        }

        if (isCloseToDestination(POTHOLE_DUNGEON_2)) {


            dungeonNumber = 1;
        }

        if (dungeonNumber == 1) {
            Microbot.getWalker().hybridWalkTo(POTHOLE_DUNGEON_1, true);
            sleep(2500, 5000);
        }

        if (isCloseToDestination(POTHOLE_DUNGEON_1)) {
            Microbot.status = "Interacting with rocks";

            dungeonNumber = 6;
        }
        //status = Status.READY_FOR_COMBAT;
    }

    private WorldPoint getPlayerLocation() {
        Player player = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getLocalPlayer());
        if (player != null) {
            return player.getWorldLocation();
        }
        return null; // Handle the null case appropriately
    }




}