package net.tiew.operationWild.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.custom.living.*;
import net.tiew.operationWild.entity.variants.*;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.utils.OWUtils;

import java.util.List;
import java.util.Objects;

public class AnimalSoulItem extends Item {

    public AnimalSoulItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public void saveEntityType(UseOnContext context, Component type) {
        context.getItemInHand().set(OWDataComponentTypes.TAMED_ENTITY_TYPE.get(), type);
    }
    public Component getEntityType(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.TAMED_ENTITY_TYPE.get()) != null) {
            return stack.get(OWDataComponentTypes.TAMED_ENTITY_TYPE.get());
        }
        return Component.literal("");
    }

    public void saveEntityOwner(UseOnContext context, Component type) {
        context.getItemInHand().set(OWDataComponentTypes.TAMED_ENTITY_OWNER.get(), type);
    }
    public Component getEntityOwner(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.TAMED_ENTITY_OWNER.get()) != null) {
            return stack.get(OWDataComponentTypes.TAMED_ENTITY_OWNER.get());
        }
        return Component.literal("");
    }

    public void saveEntityGender(UseOnContext context, boolean isMale) {
        context.getItemInHand().set(OWDataComponentTypes.TAMED_ENTITY_GENDER.get(), isMale);
    }
    public boolean getEntityGender(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.TAMED_ENTITY_GENDER.get()) != null) {
            return stack.get(OWDataComponentTypes.TAMED_ENTITY_GENDER.get());
        }
        return false;
    }

    public void saveEntityMaxHealth(UseOnContext context, float maxHealth) {
        context.getItemInHand().set(OWDataComponentTypes.TAMED_ENTITY_MAX_HEALTH.get(), maxHealth);
    }
    public float getEntityMaxHealth(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.TAMED_ENTITY_MAX_HEALTH.get()) != null) {
            return stack.get(OWDataComponentTypes.TAMED_ENTITY_MAX_HEALTH.get());
        }
        return 1.0f;
    }

    public void saveEntityDamages(UseOnContext context, float damages) {
        context.getItemInHand().set(OWDataComponentTypes.TAMED_ENTITY_DAMAGES.get(), damages);
    }
    public float getEntityDamages(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.TAMED_ENTITY_DAMAGES.get()) != null) {
            return stack.get(OWDataComponentTypes.TAMED_ENTITY_DAMAGES.get());
        }
        return 1.0f;
    }

    public void saveEntitySpeed(UseOnContext context, float speed) {
        context.getItemInHand().set(OWDataComponentTypes.TAMED_ENTITY_SPEED.get(), speed);
    }
    public float getEntitySpeed(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.TAMED_ENTITY_SPEED.get()) != null) {
            return stack.get(OWDataComponentTypes.TAMED_ENTITY_SPEED.get());
        }
        return 1.0f;
    }

    public void saveEntityScale(UseOnContext context, float speed) {
        context.getItemInHand().set(OWDataComponentTypes.TAMED_ENTITY_SCALE.get(), speed);
    }
    public float getEntityScale(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.TAMED_ENTITY_SCALE.get()) != null) {
            return stack.get(OWDataComponentTypes.TAMED_ENTITY_SCALE.get());
        }
        return 1.0f;
    }

    public void saveEntityLevel(UseOnContext context, int level) {
        context.getItemInHand().set(OWDataComponentTypes.TAMED_ENTITY_LEVEL.get(), level);
    }
    public int getEntityLevel(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.TAMED_ENTITY_LEVEL.get()) != null) {
            return stack.get(OWDataComponentTypes.TAMED_ENTITY_LEVEL.get());
        }
        return 1;
    }

    public void saveEntityVariant(UseOnContext context, int level) {
        context.getItemInHand().set(OWDataComponentTypes.TAMED_ENTITY_VARIANT.get(), level);
    }
    public int getEntityVariant(ItemStack stack) {
        if (stack.get(OWDataComponentTypes.TAMED_ENTITY_VARIANT.get()) != null) {
            return stack.get(OWDataComponentTypes.TAMED_ENTITY_VARIANT.get());
        }
        return 1;
    }


    public Component showEntityType(ItemStack stack) {
        Component entityTypeComponent = getEntityType(stack);
        if (entityTypeComponent == null || entityTypeComponent.getString().isEmpty())
            return Component.translatable("");

        String entityTypeStr = entityTypeComponent.getString();
        String entityName = entityTypeStr.toLowerCase().split("entity")[0];

        return Component.translatable("entity.ow." + entityName).setStyle(Style.EMPTY.withBold(true).withColor(chooseEntityColor(stack)));
    }

    public String showEntitySpeed(ItemStack stack, Level level) {
        Component entityTypeComponent = getEntityType(stack);
        if (entityTypeComponent == null || entityTypeComponent.getString().isEmpty())
            return "";

        return String.valueOf(Math.round(OWUtils.getSpeedBlocksPerSecond(getOWEntity(stack, level)) * 1000) / 1000.0);
    }

    private OWEntity getOWEntity(ItemStack stack, Level level) {
        OWEntity owEntity;
        switch (getEntityType(stack).getString()) {
            case "TigerEntity" -> owEntity = OWEntityRegistry.TIGER.get().create(level);
            case "BoaEntity" -> owEntity = OWEntityRegistry.BOA.get().create(level);
            case "PeacockEntity" -> owEntity = OWEntityRegistry.PEACOCK.get().create(level);
            case "TigerSharkEntity" -> owEntity = OWEntityRegistry.TIGER_SHARK.get().create(level);
            case "ElephantEntity" -> owEntity = OWEntityRegistry.ELEPHANT.get().create(level);
            case "KodiakEntity" -> owEntity = OWEntityRegistry.KODIAK.get().create(level);
            default -> owEntity = OWEntityRegistry.TIGER.get().create(level);
        }

        return owEntity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            OWEntity owEntity = getOWEntity(stack, level);
            int variantId = getEntityVariant(stack);

            if (owEntity != null) {
                float pitch = (float) OWUtils.generateRandomInterval(0.9f, 1.1f);
                owEntity.setOwnerUUID(Objects.requireNonNull(((ServerLevel) player.level()).getServer().getPlayerList().getPlayerByName(getEntityOwner(stack).getString())).getUUID());
                owEntity.setTame(true, player);
                owEntity.setLevelPoints(0);
                owEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(getEntityMaxHealth(stack));
                owEntity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getEntityDamages(stack));
                owEntity.setDamageToClient(getEntityDamages(stack));
                owEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(getEntitySpeed(stack));
                owEntity.setGender(getEntityGender(stack) ? 1 : 0);
                owEntity.setLevel(getEntityLevel(stack));
                owEntity.setScale(getEntityScale(stack));
                owEntity.setHealth(owEntity.getMaxHealth());
                owEntity.setBaseHealth((float) owEntity.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
                owEntity.setBaseDamage((float) owEntity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
                owEntity.setBaseSpeed((float) owEntity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
                owEntity.setResurrectionMaxTimer(((int) (125 * owEntity.getMaxHealth() * owEntity.getDamage() * (1 + 10 * owEntity.getSpeed()) * ((float) Math.sqrt(owEntity.getLevel() + 1) / 5f + 1))) / 2);

                if (owEntity instanceof TigerEntity tiger) {
                    TigerVariant variant = TigerVariant.byId(variantId);
                    tiger.setVariant(variant);
                    tiger.setInitialVariant(variant);
                } else if (owEntity instanceof BoaEntity boa) {
                    BoaVariant variant = BoaVariant.byId(variantId);
                    boa.setVariant(variant);
                    boa.setInitialVariant(variant);
                } else if (owEntity instanceof PeacockEntity peacock) {
                    PeacockVariant variant = PeacockVariant.byId(variantId);
                    peacock.setVariant(variant);
                    peacock.setInitialVariant(variant);
                } else if (owEntity instanceof ElephantEntity elephant) {
                    ElephantVariant variant = ElephantVariant.byId(variantId);
                    elephant.setVariant(variant);
                    elephant.setInitialVariant(variant);
                } else if (owEntity instanceof KodiakEntity kodiak) {
                    KodiakVariant variant = KodiakVariant.byId(variantId);
                    kodiak.setVariant(variant);
                    kodiak.setInitialVariant(variant);
                }

                owEntity.setPos(player.getX(), player.getY(), player.getZ());

                owEntity.setResurrection(true);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        OWSounds.TAME_SUCCESS.get(), SoundSource.PLAYERS, 1.5F, pitch);
                level.addFreshEntity(owEntity);

                owEntity.setCanDropSoul(false);
            }
        }

        if (!player.getAbilities().instabuild && !player.isCreative()) {
            stack.shrink(1);
        }
        return super.use(level, player, hand);
    }

    private int chooseEntityColor(ItemStack stack) {
        Component entityTypeComponent = getEntityType(stack);
        if (entityTypeComponent == null || entityTypeComponent.getString().isEmpty())
            return 0xFFFFFF;

        String entityTypeStr = entityTypeComponent.getString();

        return switch (entityTypeStr) {
            case "TigerEntity" -> 0xc47037;
            case "BoaEntity" -> 0x838549;
            case "PeacockEntity" -> 0x464bc1;
            case "TigerSharkEntity" -> 0x565047;
            case "ElephantEntity" -> 8749692;
            case "KodiakEntity" -> 8215109;
            default -> 0xFFFFFF;
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.ow.animal_soul", showEntityType(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        Component entityType = Component.literal(showEntityType(stack).getString())
                .withStyle(Style.EMPTY.withBold(true).withColor(chooseEntityColor(stack)))
                .append(Component.literal(" | ")
                        .withStyle(Style.EMPTY.withBold(false).withColor(0xFFFFFF)))
                .append(Component.translatable("tooltip.lvlImage")
                        .withStyle(Style.EMPTY.withBold(false).withColor(0xFFFFFF)))
                .append(Component.literal(String.valueOf(getEntityLevel(stack)))
                        .withStyle(Style.EMPTY.withBold(true).withColor(getEntityLevel(stack) >= 50 ? 0xdd9847 : 0xb8e45a)))
                .append(Component.literal(" | ")
                        .withStyle(Style.EMPTY.withBold(false).withColor(0xFFFFFF)))
                .append(Component.translatable(getEntityGender(stack) ? "tooltip.genderMale" : "tooltip.genderFemale")
                        .withStyle(Style.EMPTY.withItalic(true).withColor(getEntityGender(stack) ? 0x4647ce : 0xcb3eb3)));

        Component entityMaxHealth = Component.translatable("imageHealth")
                .withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(" " + String.valueOf(Math.round(getEntityMaxHealth(stack) * 2) / 2.0))
                        .withStyle(Style.EMPTY.withBold(false)))
                .append(Component.translatable("tooltip.HP").withStyle(Style.EMPTY.withBold(false)));
        Component entityDamages = Component.translatable("imageDamages")
                .withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(" " + String.valueOf(Math.round(getEntityDamages(stack) * 10) / 10.0))
                        .withStyle(Style.EMPTY.withBold(false)));
        Component entitySpeed = Component.translatable("imageSpeed")
                .withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(" " + String.valueOf(showEntitySpeed(stack, context.level())))
                        .withStyle(Style.EMPTY.withBold(false)))
                .append(Component.translatable("tooltip.entitySpeed").withStyle(Style.EMPTY.withBold(false)));

        Component entityOwner = Component.translatable("tooltip.ownerImage")
                .withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(getEntityOwner(stack).getString())
                        .withStyle(Style.EMPTY.withBold(false)));

        tooltip.add(entityType);
        tooltip.add(Component.nullToEmpty("-----"));
        tooltip.add(entityMaxHealth);
        tooltip.add(entityDamages);
        tooltip.add(entitySpeed);
        tooltip.add(entityOwner);
        tooltip.add(Component.nullToEmpty("-----"));
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }
}
