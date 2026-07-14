package net.tiew.operationWild.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.component.SoulData;

import java.util.List;

/**
 * Item Âme : porte le {@link SoulData} d'un compagnon mort (capturé génériquement par
 * {@code OWEntity.captureSoul()}). Un clic droit n'ouvre PAS une résurrection instantanée :
 * il ouvre l'Écran de Communion d'où le joueur lance le Rituel de Communion (défense par
 * vagues). Voir {@code ResurrectionRitual}.
 */
public class AnimalSoulItem extends Item {

    public AnimalSoulItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public static SoulData getSoul(ItemStack stack) {
        SoulData data = stack.get(OWDataComponentTypes.SOUL_DATA.get());
        return data == null ? SoulData.EMPTY : data;
    }

    private static String speciesPath(SoulData data) {
        return data.entityType() == null ? "" : data.entityType().getPath();
    }

    private static int speciesColor(SoulData data) {
        return switch (speciesPath(data)) {
            case "tiger" -> 0xC47037;
            case "boa" -> 0x838549;
            case "peacock" -> 0x464BC1;
            case "kodiak" -> 0x7D4A05;
            case "crocodile" -> 0x5E7242;
            case "orca" -> 0x3A4A5A;
            default -> 0xFFFFFF;
        };
    }

    private Component speciesName(SoulData data) {
        if (data.isEmpty() || speciesPath(data).isEmpty()) return Component.empty();
        return Component.translatable("entity.ow." + speciesPath(data))
                .setStyle(Style.EMPTY.withBold(true).withColor(speciesColor(data)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SoulData data = getSoul(stack);
        if (data.isEmpty()) return InteractionResultHolder.pass(stack);

        // Pendant un rituel en cours, l'Âme n'est pas utilisable (pas d'ouverture d'écran).
        if (net.tiew.operationWild.gui.ClientRitualState.active) return InteractionResultHolder.pass(stack);

        // L'Amulette de Résurrection doit être portée : sinon, l'item ne fait rien (aucun écran).
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean wearingAmulet = !chest.isEmpty()
                && chest.is(net.tiew.operationWild.item.OWItems.RESURRECTION_AMULET.get());
        if (!wearingAmulet) return InteractionResultHolder.pass(stack);

        // L'écran de communion est purement client : il enverra le StartRitualPacket à la validation.
        // On passe par un hook client dédié (OWClientHooks) pour ne PAS référencer de classe Screen
        // directement ici : sinon la vérification du bytecode de cet item ferait planter le serveur dédié.
        if (level.isClientSide) {
            net.tiew.operationWild.client.OWClientHooks.openRitualCommunion(data);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.ow.animal_soul", speciesName(getSoul(stack)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        SoulData data = getSoul(stack);
        if (data.isEmpty()) {
            super.appendHoverText(stack, context, tooltip, tooltipFlag);
            return;
        }

        Component header = speciesName(data)
                .copy()
                .append(Component.literal(" | ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                .append(Component.translatable("tooltip.lvlImage").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                .append(Component.literal(String.valueOf(data.level()))
                        .withStyle(Style.EMPTY.withBold(true).withColor(data.level() >= 50 ? 0xDD9847 : 0xB8E45A)))
                .append(Component.literal(" | ").withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                .append(Component.translatable(data.male() ? "tooltip.genderMale" : "tooltip.genderFemale")
                        .withStyle(Style.EMPTY.withItalic(true).withColor(data.male() ? 0x4647CE : 0xCB3EB3)));

        Component health = Component.translatable("imageHealth").withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(" " + (Math.round(data.maxHealth() * 2) / 2.0)).withStyle(Style.EMPTY))
                .append(Component.translatable("tooltip.HP").withStyle(Style.EMPTY));
        Component damage = Component.translatable("imageDamages").withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(" " + (Math.round(data.damage() * 10) / 10.0)).withStyle(Style.EMPTY));
        Component speed = Component.translatable("imageSpeed").withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(" " + (Math.round(data.speed() * 1000) / 1000.0)).withStyle(Style.EMPTY))
                .append(Component.translatable("tooltip.entitySpeed").withStyle(Style.EMPTY));

        Component owner = Component.translatable("tooltip.ownerImage").withStyle(Style.EMPTY.withBold(true))
                .append(Component.literal(data.ownerName()).withStyle(Style.EMPTY));

        tooltip.add(header);
        if (!data.nickname().isEmpty()) {
            tooltip.add(Component.literal("\"" + data.nickname() + "\"").withStyle(Style.EMPTY.withItalic(true).withColor(0xC0C0C0)));
        }
        tooltip.add(Component.literal("-----"));
        tooltip.add(health);
        tooltip.add(damage);
        tooltip.add(speed);
        tooltip.add(owner);
        tooltip.add(Component.literal("-----"));
        tooltip.add(Component.translatable("tooltip.ow.soul_hint").withStyle(Style.EMPTY.withItalic(true).withColor(0x86DBFF)));
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }
}
