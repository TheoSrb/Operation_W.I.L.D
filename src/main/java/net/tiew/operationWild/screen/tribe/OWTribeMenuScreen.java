package net.tiew.operationWild.screen.tribe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.tiew.operationWild.client.OWClientTribeData;

/**
 * Écran-répartiteur du menu de tribu (touche T). Court instant de chargement puis bascule vers le
 * dashboard (chef/membre) ou la liste des tribus.
 */
public class OWTribeMenuScreen extends OWTribeScreen {

    public OWTribeMenuScreen() {
        super(Component.translatable("owteams.menu.title"));
    }

    @Override
    public void tick() {
        super.tick();
        if (OWClientTribeData.isReceived()) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(OWClientTribeData.hasTribe()
                    ? new OWTribeDashboardScreen()
                    : new OWTribeListScreen());
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        drawPanel(g, mouseX, mouseY, partial);
        drawHeader(g, Component.translatable("owteams.menu.title"));
        g.drawCenteredString(this.font, Component.translatable("owteams.menu.loading"),
                leftPos + IMG_W / 2, topPos + IMG_H / 2 - 4, 0x707070);
        super.render(g, mouseX, mouseY, partial);
    }
}
