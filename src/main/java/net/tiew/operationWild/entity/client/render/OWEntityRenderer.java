package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;

public abstract class OWEntityRenderer<T extends OWEntity, M extends EntityModel<T>> extends MobRenderer<T, M> {

    protected static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    /**
     * Quand true, renderEntityInfo ne dessine RIEN (nom, niveau, proprietaire, equipe,
     * images d'info...). Active par les ecrans qui affichent une entite en GUI (ex:
     * preview de skin dans OWSkinsInterface) ou ces infos au-dessus de la tete n'ont pas
     * de sens. Les ecrans le passent a true autour de leur appel de rendu puis le
     * remettent a false (cf. BoaModel.RENDER_FULL_BODY).
     */
    public static boolean SUPPRESS_INFO_IN_GUI = false;

    /**
     * Mode "fantôme spectral" GÉNÉRIQUE : quand actif, TOUT renderer héritant de cette classe
     * dessine le corps de l'entité avec un {@link RenderType} translucide. Combiné au
     * {@code GhostVertexConsumer} qui force l'alpha et la teinte d'âme, ça produit un rendu
     * fantôme pour n'importe quel OWEntity sans texture ni code dédié. Utilisé par le rituel
     * de résurrection ({@code SoulGhostRenderer}).
     */
    public static boolean RENDER_AS_GHOST = false;

    public abstract double distanceToShowRealInfos();

    public abstract double infosUpOffset();

    public OWEntityRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Avalée par une orque : la bête n'existe plus à l'écran, informations comprises.
        //
        // Le contrôle est ici et pas seulement sur l'événement de rendu : celui-ci est émis au
        // cœur de super.render, alors que renderEntityInfo est appelé APRÈS, depuis cette méthode.
        // L'annuler masquait donc le corps mais laissait le niveau, le propriétaire, la bannière de
        // tribu et les jauges flotter au-dessus de la tête de l'orque.
        if (net.tiew.operationWild.entity.animals.aquatic.OrcaEntity.isSwallowed(entity)) return;

        float scale = entity.getScale();
        float babyScale = scale / 2.25f;
        Player player = entity.level().getNearestPlayer(entity, 64.0D);

        poseStack.pushPose();

        if (entity.isBaby()) {
            float maturationPercent = (float) entity.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            if (!(entity instanceof BoaEntity)) {
                poseStack.scale(currentScale, currentScale, currentScale);
            }
        } else {
            if (!(entity instanceof BoaEntity)) {
                poseStack.scale(scale, scale, scale);
            }
        }

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        renderEntityInfo(entity, poseStack, bufferSource, packedLight, player);
    }


    protected void renderEntityInfo(T entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Player player) {
        if (SUPPRESS_INFO_IN_GUI) return;
        if (!entity.isInResurrection()) {
            if (entity.isAlive() && !entity.isVehicle()) {
                if (entity.isTame()) {
                    if (entity.isBaby() && player != null) {
                        OWRendererUtils.displayTimeLeftBeforeBabyTaskAboveEntity(entity, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, entity.distanceTo(player) > 4 ? 0 : Minecraft.getInstance().options.hideGui ? 0 : 0.75f);
                        OWRendererUtils.displayImageAboveEntity(ICONS, getIconX(entity), getIconY(entity), 48, 256, -2.5f, entity.distanceTo(player) > 4 ? 2.6f : Minecraft.getInstance().options.hideGui ? 2.6f : 5.5f, entity, poseStack, bufferSource, packedLight, false);
                    }
                    if (player != null && entity.distanceTo(player) > distanceToShowRealInfos()) {
                        if (entity.currentTeam != null && !Minecraft.getInstance().options.hideGui) {
                            OWRendererUtils.displayTeamBannerAboveEntity(entity, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, infosUpOffset() + 1.2);
                        }
                        OWRendererUtils.displayOwnerAboveEntity(entity, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, infosUpOffset());
                        OWRendererUtils.displayLevelAboveEntity(entity, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, infosUpOffset());
                    }
                } else {
                    if (entity.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(entity, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(entity, poseStack, bufferSource, packedLight, 0, 1.0f + infosUpOffset(), 0, 0, (int) (distanceToShowRealInfos() - 1));
    }

    @Override
    protected RenderType getRenderType(T entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (RENDER_AS_GHOST) {
            return RenderType.entityTranslucent(this.getTextureLocation(entity));
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    protected int getIconX(T entity) {
        return 0;
    }

    protected int getIconY(T entity) {
        return 154;
    }

    @Override
    protected void renderNameTag(T entity, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {

    }
}