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
import net.tiew.operationWild.core.OWDatasSave;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.networking.packets.to_server.CheckManuscriptEntityPacket;
import net.tiew.operationWild.screen.player.adventurer_manuscript.AdventurerManuscriptScreen;

import java.util.List;

public class ManuscriptFragmentItem extends Item {

    private static final List<EntityType<? extends OWEntity>> OW_ENTITIES = List.of(
            OWEntityRegistry.CROCODILE.get(),
            OWEntityRegistry.KODIAK.get()
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
                    boolean hasEntity = OWDatasSave.owDatas.containsKey(entityName);

                    if (hasEntity) {
                        OWEntity entity = entityType.create(level);
                        player.displayClientMessage(
                                Component.translatable("tooltip.haveEntityChapter",
                                                Component.translatable(String.valueOf(entityType))
                                                        .setStyle(Style.EMPTY.withColor(entity != null ? 0xFF0000 : 0xFFFFFF)))
                                        .setStyle(Style.EMPTY.withColor(0xFF0000)),
                                true
                        );

                        return InteractionResultHolder.fail(stack);
                    }

                    PacketDistributor.sendToServer(new CheckManuscriptEntityPacket(entityName, hasEntity));

                    return InteractionResultHolder.success(stack);
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    private String getSimpleNameFromEntityType(EntityType<? extends OWEntity> entityType) {
        if (entityType == OWEntityRegistry.KODIAK.get()) return "kodiak";
        if (entityType == OWEntityRegistry.CROCODILE.get()) return "crocodile";
        return "unknown";
    }
}