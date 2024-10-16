package net.runelite.client.plugins.microbot.jogrefighter;

import net.runelite.client.plugins.microbot.jogrefighter.model.Monster;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.jogrefighter.combat.AttackNpcScript.attackableNpcs;
import static net.runelite.client.plugins.microbot.jogrefighter.combat.FlickerScript.currentMonstersAttackingUs;

public class JogreFighterOverlay extends OverlayPanel {

    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private JogreFighterOverlay(ModelOutlineRenderer modelOutlineRenderer) {
        this.modelOutlineRenderer = modelOutlineRenderer;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
        setNaughty();
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        if (attackableNpcs == null) return null;

        for (net.runelite.api.NPC npc :
                attackableNpcs) {
            if (npc != null && npc.getCanvasTilePoly() != null) {
                try {
                    graphics.setColor(Color.CYAN);
                    modelOutlineRenderer.drawOutline(npc, 2, Color.RED, 4);
                    graphics.draw(npc.getCanvasTilePoly());
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }

        for (Monster currentMonster: currentMonstersAttackingUs) {
            if (currentMonster != null && currentMonster.npc != null && currentMonster.npc.getCanvasTilePoly() != null) {
                try {
                    graphics.setColor(Color.CYAN);
                    modelOutlineRenderer.drawOutline(currentMonster.npc, 2, Color.RED, 4);
                    graphics.draw(currentMonster.npc.getCanvasTilePoly());
                    graphics.drawString("" + currentMonster.lastAttack,
                            (int) currentMonster.npc.getCanvasTilePoly().getBounds().getCenterX(),
                            (int) currentMonster.npc.getCanvasTilePoly().getBounds().getCenterY());
                } catch(Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }

        return super.render(graphics);
    }
}