package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.attacks.OWAttack;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.attacks.OWChargedAttack;
import net.tiew.operationWild.networking.packets.to_server.OWAttackPacket;

import java.util.List;

public class OWAttacksOverlay {

    private static final ResourceLocation TEXTURE = OWAttackCards.ATLAS;

    private static final int CARD_SIZE    = OWAttackCards.CARD_SIZE;
    private static final int TEX_SIZE     = OWAttackCards.TEX_SIZE;
    private static final int CARD_SPACING = 25;
    private static final int OFFHAND_SLOT_WIDTH = 29;

    private static final int GLOW_BORDER_ULTIMATE = 0x40E0FF;
    private static final int GLOW_BORDER_CHARGED  = 0xFFD700;
    private static final int ERROR_CARD_COLOR = 0xFF0000;

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        Player player = mc.player;
        if (player == null) return;

        if (player.getVehicle() == null && net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity.carriesAny(player)) {
            renderTarantulaCards(g, player, screenWidth, screenHeight);
            return;
        }

        OWEntity entity = net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity
                .resolveControlledEntity(player);
        if (entity == null) return;

        boolean carriedOnShoulder = net.tiew.operationWild.entity.attacks.OWAttackLogic.isCarriedOnShoulder(entity);
        if (!carriedOnShoulder && entity.getPassengers().indexOf(player) != 0) return;

        boolean isCrocodileReadyForTaming = entity instanceof CrocodileEntity croc
                && croc.crocodileBehaviorHandler.isReadyForTaming() && !croc.isTame();
        if (!player.getUUID().equals(entity.getOwnerUUID()) && !isCrocodileReadyForTaming
                && !entity.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.CONTROL)) return;

        Class<?> entityClass = entity.getClass();
        List<OWAttack> attacks = OWAttacksHandler.getActiveAttacks(entity);

        boolean hasComboCard = !carriedOnShoulder;
        int baseX = (screenWidth / 2) + 96;
        int baseY = screenHeight - 22;

        int comboTexX = OWAttacksHandler.getComboCardTexX(entityClass);
        int comboTexY = OWAttacksHandler.getComboCardTexY(entityClass);

        boolean isGrabbing = entity instanceof CrocodileEntity && entity.isGrabbing();

        if (hasComboCard) applyCardScale(g, baseX, baseY, OWAttackLogic.getComboClickScale(), () -> {
            if (isGrabbing) {
                OWAttackCards.drawGreyed(g, baseX, baseY, comboTexX, comboTexY);
                return;
            }

            int grayH = 0;
            if (entity.attackTimer >= 1) {
                int comboTimeMax = entity.comboCardMaxTimer();
                int coloredH = Math.min(CARD_SIZE, (int)((float) CARD_SIZE / comboTimeMax * entity.attackTimer));
                grayH = CARD_SIZE - coloredH;
            }
            OWAttackCards.draw(g, baseX, baseY, comboTexX, comboTexY, grayH);

            if (OWAttackLogic.comboClickAnimInvalid && OWAttackLogic.comboClickAnimTimeMs >= 0) {
                float p = (float)(System.currentTimeMillis() - OWAttackLogic.comboClickAnimTimeMs) / 200f;
                if (p >= 0f && p < 1f) {
                    int alpha = (int)((1f - p) * 80);
                    g.fill(baseX, baseY, baseX + CARD_SIZE, baseY + CARD_SIZE,
                            (alpha << 24) | ERROR_CARD_COLOR);
                }
            }
        });

        if (attacks == null) return;

        for (int i = 0; i < attacks.size(); i++) {
            OWAttack attack = attacks.get(i);
            int cardX = baseX + (hasComboCard ? i + 1 : i) * CARD_SPACING;
            float flipProgress = OWAttacksHandler.isSecondaryCard(attack)
                    ? OWAttackLogic.getSecondaryFlipProgress(entity.getId())
                    : -1f;
            OWAttack shown = attack;
            if (flipProgress >= 0f && flipProgress < 0.5f) {
                OWAttack leaving = OWAttackPacket.getAttack(OWAttackLogic.getSecondaryFlipFromId());
                if (leaving != null) shown = leaving;
            }

            int texX = OWAttacksHandler.getCardTexX(entityClass, shown);
            int texY = OWAttacksHandler.getCardTexY(entityClass, shown);

            boolean isPrimalDive = attack.getId() == OWAttacksHandler.PRIMAL_DIVE_ID;
            boolean isTargeting  = isPrimalDive && OWAttackLogic.isCrocTargeting;
            float   grabProgress = isPrimalDive ? OWAttackLogic.getCrocGrabActiveProgress() : 0f;

            boolean isNapUltimate    = attack.getId() == OWAttacksHandler.NAP_ULTIMATE_ID;
            boolean isNapDrainActive = false;

            boolean isVenomFangs = attack.getId() == OWAttacksHandler.VENOM_FANGS_ID;
            boolean isVenomArmed = isVenomFangs && entity instanceof BoaEntity boa && boa.isVenomArmed();

            boolean isWhirlwind = attack.getId() == OWAttacksHandler.WHIRLWIND_FISTS_ID;

            boolean isHippoRoll = attack.getId() == OWAttacksHandler.HIPPO_ROLL_ID;
            boolean isHippoRolling = isHippoRoll
                    && entity instanceof net.tiew.operationWild.entity.animals.aquatic.HippopotamusEntity rollingHippo
                    && rollingHippo.isRolling();

            boolean isWaterSpray = attack.getId() == OWAttacksHandler.WATER_SPRAY_ID;

            net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity featherPeacock =
                    attack.getId() == OWAttacksHandler.FEATHER_VOLLEY_ID
                            && entity instanceof net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity fp ? fp : null;
            boolean glideBlocked = entity.isGliding() && (featherPeacock != null || attack.isUltimate());

            boolean isCharging = attack instanceof OWChargedAttack
                    && OWAttackLogic.isCharging
                    && OWAttackLogic.getCurrentAttackId() == attack.getId();

            float sharedCooldown = OWAttacksHandler.isSecondaryCard(attack)
                    ? OWAttackLogic.getSharedSecondaryProgress(entity)
                    : 0f;

            net.tiew.operationWild.entity.animals.terrestrial.LionEntity lioness =
                    entity instanceof net.tiew.operationWild.entity.animals.terrestrial.LionEntity lion && !lion.isMale() ? lion : null;
            boolean isHuntingLeap = lioness != null && attack.getId() == OWAttacksHandler.HUNTING_LEAP_ID;
            boolean isDistressCall = lioness != null && attack.getId() == OWAttacksHandler.DISTRESS_CALL_ID;

            float fillProgress;
            boolean isGlowing = false;

            if (isCharging) {
                fillProgress = OWAttackLogic.getChargeProgress();
                isGlowing    = fillProgress >= 1.0f;
            } else if (isHuntingLeap && lioness.isLatchActive()) {
                fillProgress = lioness.getLatchPhase() == net.tiew.operationWild.entity.animals.terrestrial.LionEntity.LATCH_HOLD
                        ? lioness.getLatchGripRatio() : 1.0f;
                isGlowing    = true;
            } else if (isHippoRolling) {
                fillProgress = 1.0f;
                isGlowing    = true;
            } else if (featherPeacock != null) {
                float featherCharge = OWAttackLogic.getFeatherChargeProgress();
                if (featherCharge >= 0f) {
                    fillProgress = featherCharge;
                    isGlowing    = featherCharge >= 1f;
                } else {
                    fillProgress = featherPeacock.getFeatherStock() > 0 ? 1.0f : featherPeacock.featherRegenProgress();
                    isGlowing    = featherPeacock.isFeatherAiming();
                }
            } else if (sharedCooldown > 0f) {
                fillProgress = 1.0f - sharedCooldown;
            } else if (isVenomFangs && entity instanceof BoaEntity boa) {
                if (isVenomArmed) {
                    fillProgress = 1.0f;
                    isGlowing    = true;
                } else {
                    int cd = boa.getVenomCooldownTicks();
                    fillProgress = cd > 0
                            ? 1.0f - (float) cd / OWAttacksConstants.Boa.VENOM_FANGS_COOLDOWN_TICKS
                            : 1.0f;
                }
            } else if (isWaterSpray && entity instanceof ElephantEntity elephant) {
                fillProgress = elephant.getTrunkWaterRatio();
                isGlowing    = elephant.canUseTrunkWater();
            } else if (isWhirlwind && entity instanceof KangarooEntity kangaroo) {
                if (kangaroo.isSpinning()) {
                    isGlowing = true;
                    int t    = kangaroo.clientSpinTicks;
                    int peak = OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_THRESHOLD_TICKS;
                    int max  = OWAttacksConstants.Kangaroo.WHIRLWIND_MAX_DURATION_TICKS;
                    fillProgress = t <= peak
                            ? 1.0f
                            : Math.max(0f, 1.0f - (float) (t - peak) / (max - peak));
                } else {
                    int cd = kangaroo.getWhirlwindCooldownTicks();
                    fillProgress = cd > 0
                            ? 1.0f - (float) cd / OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_TICKS
                            : 1.0f;
                }
            } else if (grabProgress > 0f) {
                fillProgress = grabProgress;
            } else if (attack.isUltimate()) {
                float activeProgress = OWAttackLogic.getUltimateActiveProgress(entity.getId(), attack.getId());
                if (activeProgress > 0f) {
                    fillProgress = activeProgress;
                    if (isNapUltimate && OWAttackLogic.isKodiakNapping) {
                        isNapDrainActive = true;
                        isGlowing        = true;
                    }
                } else {
                    if (isNapUltimate && OWAttackLogic.isKodiakNapping) {
                        OWAttackLogic.onKodiakNapExpired(entity.getId(), attack);
                    }
                    if (!attack.isUnlocked(entity)) {
                        fillProgress = attack.getUnlockProgress(entity);
                    } else {
                        float cooldownRemaining = OWAttackLogic.getCooldownProgress(entity.getId(), attack.getId());
                        if (cooldownRemaining > 0f) {
                            fillProgress = 1.0f - cooldownRemaining;
                        } else {
                            fillProgress = 1.0f;
                            isGlowing    = true;
                        }
                    }
                }
            } else {
                float cooldownRemaining = OWAttackLogic.getCooldownProgress(entity.getId(), attack.getId());
                fillProgress = cooldownRemaining > 0f ? 1.0f - cooldownRemaining : 1.0f;
            }

            if (isHuntingLeap && !lioness.isLatchActive() && sharedCooldown <= 0f) {
                isGlowing = lioness.getClientLeapTargetId() >= 0;
                if (!isGlowing) fillProgress = 0f;
            }

            boolean isDistressBlocked = isDistressCall && attack.isUnlocked(entity)
                    && OWAttackLogic.getUltimateActiveProgress(entity.getId(), attack.getId()) <= 0f
                    && OWAttackLogic.getCooldownProgress(entity.getId(), attack.getId()) <= 0f
                    && !lioness.isDistressReady();

            boolean isWhirlwindBlockedInWater = isWhirlwind && entity.isInWater();

            boolean isHealOrbBlocked = attack.getId() == OWAttacksHandler.HEAL_SNACK_ID
                    && carriedOnShoulder && OWAttackLogic.isHealOrbBlocked(player);

            boolean isClanCall = attack.getId() == OWAttacksHandler.CLAN_CALL_ID
                    && entity instanceof net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
            int callableLionesses = isClanCall
                    ? ((net.tiew.operationWild.entity.animals.terrestrial.LionEntity) entity).getCallableClanmateCount()
                    : 0;
            boolean isClanCallEmpty = isClanCall && callableLionesses <= 0 && !isCharging;

            boolean isTongueBlocked = attack.getId() == OWAttacksHandler.TONGUE_VINE_ID
                    && entity instanceof net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity chameleon
                    && chameleon.isMimicryActive();

            boolean isTalonsGrounded = false;
            if (attack.getId() == OWAttacksHandler.CONDOR_TALONS_ID
                    && entity instanceof net.tiew.operationWild.entity.animals.flying.CondorEntity talonCondor) {
                int talonCooldown = talonCondor.getTalonCooldownTicks();
                if (talonCondor.isCarryingPrey()) {
                    fillProgress = 1.0f;
                    isGlowing = true;
                } else if (talonCooldown > 0) {
                    fillProgress = 1.0f - (float) talonCooldown / OWAttacksConstants.Condor.TALON_COOLDOWN_TICKS;
                    isGlowing = false;
                } else {
                    fillProgress = 1.0f;
                    isGlowing = talonCondor.canUseTalons() && talonCondor.findTalonPrey() != null;
                    isTalonsGrounded = !talonCondor.isAirborneFlight();
                }
            }

            if (attack.getId() == OWAttacksHandler.GIRAFFE_BLESSING_ID
                    && entity instanceof net.tiew.operationWild.entity.animals.terrestrial.GiraffeEntity blessingGiraffe) {
                int blessingCooldown = blessingGiraffe.getBlessingCooldown();
                if (blessingGiraffe.isBlessing()) {
                    fillProgress = (float) blessingGiraffe.getBlessingTick() / OWAttacksConstants.Giraffe.BLESSING_MAX_TICKS;
                    isGlowing = true;
                } else if (blessingCooldown > 0) {
                    fillProgress = 1.0f - (float) blessingCooldown / OWAttacksConstants.Giraffe.BLESSING_COOLDOWN_TICKS;
                    isGlowing = false;
                } else {
                    fillProgress = 1.0f;
                    isGlowing = blessingGiraffe.pickBlessingTarget(player) != null;
                }
            }

            if (isGrabbing || isWhirlwindBlockedInWater || isHealOrbBlocked || isClanCallEmpty || isDistressBlocked || glideBlocked || isTongueBlocked || isTalonsGrounded) {
                fillProgress     = 0f;
                isGlowing        = false;
                isNapDrainActive = false;
            }

            final int     fColoredH = (int)(CARD_SIZE * fillProgress);
            final int     fGrayH    = CARD_SIZE - fColoredH;
            final boolean fGlowing  = isGlowing;
            final boolean fShowBorder = fGlowing || isTargeting;
            final int fBorderRgb = isTargeting
                    ? 0x00FF55
                    : (attack.isUltimate() ? GLOW_BORDER_ULTIMATE : GLOW_BORDER_CHARGED);

            float clickScale = OWAttackLogic.getAttackClickScale(attack.getId());
            float cardScale  = (fGlowing || isTargeting) && clickScale == 1f
                    ? OWAttackLogic.getGlowPulseScale()
                    : clickScale;

            boolean fInvalid = OWAttackLogic.isAttackClickInvalid(attack.getId());

            int counter = attack.isUltimate() && attack.hasUnlockCounter() && attack.isUnlocked(entity)
                    ? attack.getUnlockCount(entity) : 0;

            float flipScaleX = flipProgress >= 0f
                    ? cardScale * OWAttackLogic.getSecondaryFlipScaleX(flipProgress)
                    : cardScale;

            float flipLift = flipProgress >= 0f ? OWAttackLogic.getSecondaryFlipLift(flipProgress) : 0f;

            applyCardScale(g, cardX, baseY, flipScaleX, cardScale, flipLift, () -> {
                OWAttackCards.draw(g, cardX, baseY, texX, texY, fGrayH);
                if (fShowBorder) {
                    drawGlowBorder(g, cardX, baseY, fBorderRgb);
                }

                if (fInvalid) {
                    float p = OWAttackLogic.getClickAnimProgress(attack.getId());
                    if (p >= 0f && p < 1f) {
                        int alpha = (int)((1f - p) * 80);
                        g.fill(cardX, baseY, cardX + CARD_SIZE, baseY + CARD_SIZE,
                                (alpha << 24) | ERROR_CARD_COLOR);
                    }
                }
            });

            if (OWAttacksHandler.hasSwitchableSecondary(entity)
                    && OWAttacksHandler.isSecondaryCard(attack)) {
                drawSwitchHint(g, cardX, baseY, flipProgress);
            }

            if (attack.isUltimate() && attack.hasUnlockCounter()) {
                noteUnlockCounter(entity.getId(), counter);
                drawUnlockPop(g, cardX, baseY, entity.getId());
            }

            if (isClanCall) {
                drawClanCallBadge(g, cardX, baseY, callableLionesses);
            }

            if (isDistressCall) {
                drawClanCallBadge(g, cardX, baseY, lioness.getDistressResponderCount());
            }

            if (featherPeacock != null) {
                drawClanCallBadge(g, cardX, baseY, featherPeacock.getFeatherStock());
            }
        }

    }

    private static void renderTarantulaCards(GuiGraphics g, Player player, int screenWidth, int screenHeight) {
        OWAttack web = OWAttacksHandler.TarantulaAttacks.WEB_GRAPPLE;
        Class<?> entityClass = net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity.class;
        int texX = OWAttacksHandler.getCardTexX(entityClass, web);
        int texY = OWAttacksHandler.getCardTexY(entityClass, web);
        int baseX = (screenWidth / 2) + 96;
        int baseY = screenHeight - 22;
        int[] slots = {net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity.SLOT_LEFT,
                net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity.SLOT_RIGHT};
        int offhandShift = player.getOffhandItem().isEmpty() ? 0 : OFFHAND_SLOT_WIDTH;
        int[] cardXs = {(screenWidth / 2) - 96 - CARD_SIZE - offhandShift, baseX};

        for (int i = 0; i < slots.length; i++) {
            var tarantula = net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity.carriedOn(player, slots[i]);
            if (tarantula == null) continue;
            if (!player.getUUID().equals(tarantula.getOwnerUUID())
                    && !tarantula.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.CONTROL)) continue;

            int cardX = cardXs[i];

            boolean attached = tarantula.isWebAttached();
            boolean lacksEnergy = tarantula.getVitalEnergy() > tarantula.getVitalEnergyCapacity() - web.getEnergyRequired();
            int cooldown = tarantula.getWebCooldown();
            float fillProgress;
            if (attached || tarantula.isWebOut()) fillProgress = 1.0f;
            else if (tarantula.isEating()) fillProgress = 1.0f - (float) tarantula.getEatTimer() / OWAttacksConstants.Tarantula.EAT_TICKS;
            else if (!tarantula.isWebLoaded() || lacksEnergy) fillProgress = 0f;
            else fillProgress = 1.0f - (float) cooldown / OWAttacksConstants.Tarantula.WEB_COOLDOWN_TICKS;
            int grayH = CARD_SIZE - (int) (CARD_SIZE * Mth.clamp(fillProgress, 0f, 1f));
            float clickScale = OWAttackLogic.getAttackClickScale(web.getId());
            float cardScale = attached && clickScale == 1f ? OWAttackLogic.getGlowPulseScale() : clickScale;
            boolean invalid = OWAttackLogic.isAttackClickInvalid(web.getId());

            applyCardScale(g, cardX, baseY, cardScale, () -> {
                OWAttackCards.draw(g, cardX, baseY, texX, texY, grayH);
                if (attached) drawGlowBorder(g, cardX, baseY, GLOW_BORDER_CHARGED);
                if (invalid) {
                    float p = OWAttackLogic.getClickAnimProgress(web.getId());
                    if (p >= 0f && p < 1f) {
                        int alpha = (int) ((1f - p) * 80);
                        g.fill(cardX, baseY, cardX + CARD_SIZE, baseY + CARD_SIZE, (alpha << 24) | ERROR_CARD_COLOR);
                    }
                }
            });

        }
    }

    private static void drawClanCallBadge(GuiGraphics g, int cardX, int baseY, int count) {
        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        String text = String.valueOf(count);
        int color = count > 0 ? 0xFFE8B04A : 0xFFFF5555;
        float scale = 0.75f;

        g.pose().pushPose();
        g.pose().translate(cardX + CARD_SIZE - font.width(text) * scale - 1, baseY + CARD_SIZE - font.lineHeight * scale + 1, 300);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text, 0, 0, color, true);
        g.pose().popPose();
    }

    private static final int SWITCH_ICON_U = 0;
    private static final int SWITCH_ICON_V = 247;
    private static final int SWITCH_ICON_W = 10;
    private static final int SWITCH_ICON_H = 9;
    private static final long COUNTER_POP_MS = 950L;
    private static final float COUNTER_POP_RISE = 9f;
    private static final float COUNTER_POP_SCALE = 0.78f;
    private static final int COUNTER_POP_OFFSET_X = 4;
    private static final int COUNTER_POP_OFFSET_Y = 1;

    private static int counterPopEntityId = -1;
    private static int counterPopValue = 0;
    private static int counterPopSeen = -1;
    private static long counterPopStartMs = 0L;

    private static final int HINT_OFFSET_X = 3;
    private static final int HINT_OFFSET_Y = 2;

    private static void noteUnlockCounter(int entityId, int count) {
        if (entityId != counterPopEntityId) {
            counterPopEntityId = entityId;
            counterPopSeen = count;
            counterPopStartMs = 0L;
            return;
        }

        if (count > counterPopSeen) {
            counterPopValue = count;
            counterPopStartMs = System.currentTimeMillis();
        }
        counterPopSeen = count;
    }

    private static void drawUnlockPop(GuiGraphics g, int cardX, int cardY, int entityId) {
        if (entityId != counterPopEntityId || counterPopValue <= 0) return;

        long age = System.currentTimeMillis() - counterPopStartMs;
        if (counterPopStartMs == 0L || age >= COUNTER_POP_MS) return;

        float life = age / (float) COUNTER_POP_MS;
        int alpha = (int) (net.minecraft.util.Mth.clamp((1f - life) * 2.2f, 0f, 1f) * 255f);
        if (alpha <= 8) return;

        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        String text = "+" + counterPopValue;

        float x = cardX + CARD_SIZE + COUNTER_POP_OFFSET_X - font.width(text) * COUNTER_POP_SCALE;
        float y = cardY + CARD_SIZE + COUNTER_POP_OFFSET_Y
                - font.lineHeight * COUNTER_POP_SCALE - COUNTER_POP_RISE * life;

        g.pose().pushPose();
        g.pose().translate(x, y, 0f);
        g.pose().scale(COUNTER_POP_SCALE, COUNTER_POP_SCALE, 1f);
        g.drawString(font, text, 0, 0, (alpha << 24) | 0xFFFFFF, true);
        g.pose().popPose();
    }

    private static void drawSwitchHint(GuiGraphics g, int cardX, int cardY, float flipProgress) {
        int x = cardX + CARD_SIZE - SWITCH_ICON_W + HINT_OFFSET_X;
        int y = cardY + CARD_SIZE - SWITCH_ICON_H + HINT_OFFSET_Y;

        if (flipProgress < 0f) {
            g.blit(TEXTURE, x, y, SWITCH_ICON_U, SWITCH_ICON_V,
                    SWITCH_ICON_W, SWITCH_ICON_H, TEX_SIZE, TEX_SIZE);
            return;
        }

        float angle = OWAttackLogic.getSecondaryFlipIconAngle(flipProgress);
        float lift = OWAttackLogic.getSecondaryFlipLift(flipProgress);
        float cx = x + SWITCH_ICON_W / 2f;
        float cy = y + SWITCH_ICON_H / 2f;

        g.pose().pushPose();
        g.pose().translate(cx, cy - lift, 0f);
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        g.pose().translate(-cx, -cy, 0f);
        g.blit(TEXTURE, x, y, SWITCH_ICON_U, SWITCH_ICON_V,
                SWITCH_ICON_W, SWITCH_ICON_H, TEX_SIZE, TEX_SIZE);
        g.pose().popPose();
    }

    private static void drawGlowBorder(GuiGraphics g, int cardX, int cardY, int rgbColor) {
        long t = System.currentTimeMillis();
        float pulse = (float)(Math.sin(t / 400.0 * Math.PI * 2) * 0.5 + 0.5);
        int alpha = (int)(pulse * 180) + 75;
        int color = (alpha << 24) | (rgbColor & 0x00FFFFFF);
        g.fill(cardX - 1, cardY - 1, cardX + CARD_SIZE + 1, cardY,                    color);
        g.fill(cardX - 1, cardY + CARD_SIZE, cardX + CARD_SIZE + 1, cardY + CARD_SIZE + 1, color);
        g.fill(cardX - 1, cardY,             cardX,                  cardY + CARD_SIZE, color);
        g.fill(cardX + CARD_SIZE, cardY,     cardX + CARD_SIZE + 1,  cardY + CARD_SIZE, color);
    }

    private static void applyCardScale(GuiGraphics g, int cardX, int cardY, float scale, Runnable draw) {
        applyCardScale(g, cardX, cardY, scale, scale, 0f, draw);
    }

    private static void applyCardScale(GuiGraphics g, int cardX, int cardY,
                                       float scaleX, float scaleY, float liftY, Runnable draw) {
        if (scaleX == 1f && scaleY == 1f && liftY == 0f) { draw.run(); return; }
        float cx = cardX + CARD_SIZE / 2f;
        float cy = cardY + CARD_SIZE / 2f;
        g.pose().pushPose();
        g.pose().translate(cx, cy - liftY, 0f);
        g.pose().scale(scaleX, scaleY, 1f);
        g.pose().translate(-cx, -cy, 0f);
        draw.run();
        g.pose().popPose();
    }
}
