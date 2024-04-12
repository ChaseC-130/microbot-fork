package net.runelite.client.plugins.microbot.jogrefighter.combat;

public class GlobalState {
    public static volatile Status status = Status.STARTUP;

    public enum Status {
        READY_FOR_COMBAT,
        READY_FOR_BANK,
        OTHER,
        STARTUP
    }

    public static volatile int BonesCollected = 0;
}
