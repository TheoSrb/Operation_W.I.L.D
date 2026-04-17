package net.tiew.operationWild.entity.client.skin.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.model.skin.TigerModelSkins;
import net.tiew.operationWild.entity.client.skin.TigerSkin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirusTigerSkin extends TigerSkin {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus.png");
    private static final ResourceLocation GLOW =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_glowing.png");

    private static final ResourceLocation FACE_ANGRY =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_1.png");
    private static final ResourceLocation FACE_SLEEP =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_3.png");
    private static final ResourceLocation[] FACES_IDLE = {
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_2.png"),
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_4.png"),
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_5.png"),
    };

    // Per-entity face animation state
    private final Map<UUID, Long> nextChangeTimeMap = new HashMap<>();
    private final Map<UUID, Integer> currentTextureMap = new HashMap<>();

    public VirusTigerSkin() {
        // REPLACEMENT mode: swaps model entirely
        super(Mode.REPLACEMENT, TEXTURE, null, TigerModelSkins.LAYER_VIRUS, TigerModelSkins::createSkinVirus);
    }

    @Override
    public void renderExtraLayers(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, TigerEntity tiger, TigerModel<TigerEntity> model) {
        // Always render the glowing body
        renderGlow(poseStack, bufferSource, GLOW, model);

        // Render the appropriate face
        if (tiger.isMad()) {
            renderGlow(poseStack, bufferSource, FACE_ANGRY, model);
        } else if (tiger.isSleeping() || tiger.isNapping() || tiger.isSitting()) {
            renderGlow(poseStack, bufferSource, FACE_SLEEP, model);
        } else {
            renderGlow(poseStack, bufferSource, getAnimatedFace(tiger), model);
        }
    }

    private ResourceLocation getAnimatedFace(TigerEntity tiger) {
        long now = tiger.level().getGameTime();
        UUID id = tiger.getUUID();

        if (!nextChangeTimeMap.containsKey(id) || now >= nextChangeTimeMap.get(id)) {
            int interval = tiger.getRandom().nextInt(40) + 20;
            nextChangeTimeMap.put(id, now + interval);
            currentTextureMap.put(id, tiger.getRandom().nextInt(FACES_IDLE.length));
        }

        return FACES_IDLE[currentTextureMap.getOrDefault(id, 0)];
    }
}