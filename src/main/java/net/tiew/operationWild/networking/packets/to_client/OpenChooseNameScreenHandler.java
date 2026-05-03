package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.screen.entity.OWChooseNameScreen;

@OnlyIn(Dist.CLIENT)
public class OpenChooseNameScreenHandler {
    public static void handle(OpenChooseNameScreen packet, IPayloadContext context) {
        context.enqueueWork(() ->
                Minecraft.getInstance().setScreen(new OWChooseNameScreen(packet.entityId()))
        );
    }
}