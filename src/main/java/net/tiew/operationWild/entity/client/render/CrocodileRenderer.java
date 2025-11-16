
package net.tiew.operationWild.entity.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.client.layer.CrocodileLayer;
import net.tiew.operationWild.entity.client.layer.KodiakLayer;
import net.tiew.operationWild.entity.client.layer.skins.CrocodileSkins;
import net.tiew.operationWild.entity.client.layer.skins.KodiakSkins;
import net.tiew.operationWild.entity.client.model.CrocodileModel;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.variants.CrocodileVariant;
import net.tiew.operationWild.entity.variants.KodiakVariant;

import java.util.Map;

public class CrocodileRenderer extends OWEntityRenderer<CrocodileEntity, CrocodileModel<CrocodileEntity>> {
    private static final Map<CrocodileVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(CrocodileVariant.class), map -> {
        map.put(CrocodileVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_default.png"));
        map.put(CrocodileVariant.DARK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_dark.png"));
        map.put(CrocodileVariant.GREEN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/crocodile_green.png"));
        map.put(CrocodileVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/crocodile/skin/crocodile_skin_gold.png"));
    });
    private static final ResourceLocation CROCODILE_TAMING = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/crocodile_taming.png");

    public CrocodileRenderer(EntityRendererProvider.Context context) {
        super(context, new CrocodileModel<>(context.bakeLayer(CrocodileModel.LAYER_LOCATION)), 1.2f);
        this.addLayer(new CrocodileLayer(this));
        this.addLayer(new CrocodileSkins(this));
    }

    @Override
    public ResourceLocation getTextureLocation(CrocodileEntity crocodile) {
        return LOCATION_BY_VARIANT.get(crocodile.getVariant());
    }

    @Override
    public double distanceToShowRealInfos() {
        return 3;
    }

    @Override
    public double infosUpOffset() {
        return 0;
    }

    @Override
    public void render(CrocodileEntity crocodile, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(crocodile, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        if (!crocodile.isTame() && crocodile.getSacrificesUnity() > 0 && !crocodile.isVehicle()) {
            OWRendererUtils.displayImageAboveEntity(CROCODILE_TAMING, 0, 15, 23, 21, 256, 2, -1.85f, -0.1f, 0, crocodile, poseStack, bufferSource, packedLight, true);

            int step = getSacrificesSteps(crocodile);

            OWRendererUtils.displayImageAboveEntity(CROCODILE_TAMING, 23 + ((step - 1) * 21), 15, 21, 19, 256, 1.85f, 0.05f - 1.85f, -0.1f, 0.01f, crocodile, poseStack, bufferSource, packedLight, true);
        }
    }

    private int getSacrificesSteps(CrocodileEntity crocodile) {
        int step = 0;
        float sacrificesUnity = crocodile.getSacrificesUnity();

        if (sacrificesUnity >= 100) step = 11;
        else if (sacrificesUnity >= 90) step = 10;
        else if (sacrificesUnity >= 80) step = 9;
        else if (sacrificesUnity >= 70) step = 8;
        else if (sacrificesUnity >= 60) step = 7;
        else if (sacrificesUnity >= 50) step = 6;
        else if (sacrificesUnity >= 40) step = 5;
        else if (sacrificesUnity >= 30) step = 4;
        else if (sacrificesUnity >= 20) step = 3;
        else if (sacrificesUnity >= 10) step = 2;
        else if (sacrificesUnity >= 0) step = 1;

        return step;
    }
}
