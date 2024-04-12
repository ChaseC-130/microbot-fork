package net.runelite.client.plugins.microbot.jogrefighter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(JogreFighterConfig.GROUP)
public interface JogreFighterConfig extends Config {

    String GROUP = "JogreFighter";

    @ConfigItem(
            keyName = "GUIDE",
            name = "GUIDE",
            description = "GUIDE",
            position = 0
    )
    default String GUIDE()
    {
        return "Fights Jogres in Pothole dungeon. Banks in Varrock teleport. Needs teleport to house in Rimmington and gold for Karamja.\n" +
                "Withdraws sharks or monkfish, 1 teleport to house, and 1 varrock teleport tab.\n" +
                "Start the script in Varrock or with the above mentioned items for great results.";
    }

}

