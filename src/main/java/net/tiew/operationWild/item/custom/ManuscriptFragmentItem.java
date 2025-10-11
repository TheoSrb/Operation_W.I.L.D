package net.tiew.operationWild.item.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.networking.packets.to_server.CheckManuscriptEntityPacket;
import net.tiew.operationWild.screen.player.adventurer_manuscript.AdventurerManuscriptScreen;

import java.util.List;

public class ManuscriptFragmentItem extends Item {

    private static final List<EntityType<? extends OWEntity>> OW_ENTITIES = List.of(
            OWEntityRegistry.TIGER.get(),
            OWEntityRegistry.BOA.get(),
            OWEntityRegistry.PEACOCK.get(),
            OWEntityRegistry.ELEPHANT.get(),
            OWEntityRegistry.MANDRILL.get(),
            OWEntityRegistry.CROCODILE.get(),
            OWEntityRegistry.KODIAK.get(),
            OWEntityRegistry.JELLYFISH.get(),
            OWEntityRegistry.TIGER_SHARK.get(),
            OWEntityRegistry.MANTA.get(),
            OWEntityRegistry.HYENA.get(),
            OWEntityRegistry.RED_PANDA.get(),
            OWEntityRegistry.WALRUS.get(),
            OWEntityRegistry.CHAMELEON.get()
    );

    public ManuscriptFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (stack.get(OWDataComponentTypes.MANUSCRIPT_FRAGMENT_ENTITY.get()) == null) {
            int indexChoose = level.getRandom().nextInt(OW_ENTITIES.size());
            EntityType<? extends OWEntity> chosenEntity = OW_ENTITIES.get(indexChoose);
            stack.set(OWDataComponentTypes.MANUSCRIPT_FRAGMENT_ENTITY.get(), Component.literal(getSimpleNameFromEntityType(chosenEntity)));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag);

        Component fragmentEntityComponent = stack.get(OWDataComponentTypes.MANUSCRIPT_FRAGMENT_ENTITY.get());
        if (fragmentEntityComponent != null) {
            String entityName = fragmentEntityComponent.getString();
            EntityType<? extends OWEntity> entityType = OWEntityRegistry.getEntityTypeFromName(entityName);

            Level level = context.level();
            OWEntity entity = entityType != null && level != null ? entityType.create(level) : null;

            Component entityDisplayName = entityType != null ?
                    Component.translatable(entityType.getDescriptionId()).setStyle(Style.EMPTY.withBold(true).withColor(entity != null ? entity.getEntityColor() : 0xFFFFFF)) :
                    Component.translatable("tooltip.chapterEmpty");

            Component text = Component.translatable("tooltip.chapter", entityDisplayName);
            tooltip.add(text);
        } else {
            Component text = Component.translatable("tooltip.chapter", Component.translatable("tooltip.chapterEmpty"));
            tooltip.add(text);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        Component fragmentEntityComponent = stack.get(OWDataComponentTypes.MANUSCRIPT_FRAGMENT_ENTITY.get());
        if (fragmentEntityComponent != null) {
            String entityName = fragmentEntityComponent.getString();
            EntityType<? extends OWEntity> entityType = OWEntityRegistry.getEntityTypeFromName(entityName);

            if (entityType != null) {
                if (level.isClientSide()) {
                    boolean hasEntity = AdventurerManuscriptScreen.tempMap.containsKey(entityType) ||
                            AdventurerManuscriptScreen.OW_ENTITIES.containsKey(entityType);

                    if (hasEntity) {
                        OWEntity entity = entityType.create(level);
                        player.displayClientMessage(
                                Component.translatable("tooltip.haveEntityChapter",
                                                Component.translatable(String.valueOf(entityType))
                                                        .setStyle(Style.EMPTY.withColor(entity != null ? 0xFF0000 : 0xFFFFFF)))
                                        .setStyle(Style.EMPTY.withColor(0xFF0000)),
                                true
                        );
                    } else {
                        player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    }

                    PacketDistributor.sendToServer(new CheckManuscriptEntityPacket(entityName, hasEntity));

                    return InteractionResultHolder.success(stack);
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    private String getSimpleNameFromEntityType(EntityType<? extends OWEntity> entityType) {
        if (entityType == OWEntityRegistry.TIGER_SHARK.get()) return "tiger_shark";
        if (entityType == OWEntityRegistry.TIGER.get()) return "tiger";
        if (entityType == OWEntityRegistry.BOA.get()) return "boa";
        if (entityType == OWEntityRegistry.PEACOCK.get()) return "peacock";
        if (entityType == OWEntityRegistry.HYENA.get()) return "hyena";
        if (entityType == OWEntityRegistry.KODIAK.get()) return "kodiak";
        if (entityType == OWEntityRegistry.RED_PANDA.get()) return "red_panda";
        if (entityType == OWEntityRegistry.CHAMELEON.get()) return "chameleon";
        if (entityType == OWEntityRegistry.JELLYFISH.get()) return "jellyfish";
        if (entityType == OWEntityRegistry.MANTA.get()) return "manta";
        if (entityType == OWEntityRegistry.WALRUS.get()) return "walrus";
        if (entityType == OWEntityRegistry.ELEPHANT.get()) return "elephant";
        if (entityType == OWEntityRegistry.MANDRILL.get()) return "mandrill";
        if (entityType == OWEntityRegistry.CROCODILE.get()) return "crocodile";
        return "unknown";
    }
}