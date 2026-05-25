package net.tiew.operationWild.screen.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.component.SyringeData;
import net.tiew.operationWild.entity.OWEntity;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class DNAAnalyserScreen extends AbstractContainerScreen<DNAAnalyserMenu> {

    // ── Textures ────────────────────────────────────────────────────────────────

    private static final ResourceLocation DNA_ANALYSER_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/dna_analyser_gui.png");

    /**
     * The OW inventory sprite sheet (256×256).
     * <p>
     * Known sprite positions:
     * <ul>
     *   <li>u=0-175, v=0-165 → main 176×166 background (contains the dark stat panel on the right,
     *       with the health/damage/speed icons baked in at approximately u=89,v=23 / u=89,v=41 / u=89,v=58)</li>
     *   <li>u=0, v=224 → food icon 16×16</li>
     *   <li>u=176, v=0/16/32/48/64 → tab icons 16×16</li>
     *   <li>u=240, v=… → saddle icons 16×16</li>
     * </ul>
     */
    private static final ResourceLocation OW_INVENTORY_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_inventory_gui.png");

    /**
     * mob_types.png — gender icons at (u=0, v=36) female / (u=0, v=48) male, each 12×12.
     */
    private static final ResourceLocation MISC_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    /** Paw / taming-experience icon (standalone 11×11 PNG). */
    private static final ResourceLocation TAMING_ICON_LOCATION =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                    "textures/gui/mob_page/misc/ow_entity_journal_interface_taming_experience_gui.png");

    // ── Derived from OWInventoryScreen layout ────────────────────────────────
    // OWInventoryScreen: imageWidth=176, imageHeight=166
    //   centerX = i + imageWidth/2  = i + 88
    //   centerY = j + imageHeight/2 = j + 83
    //
    //   Health text drawn at: centerX + 12 = i + 100,  centerY - 60 = j + 23
    //   Damage text drawn at: centerX + 12 = i + 100,  centerY - 42 = j + 41
    //   Speed  text drawn at: centerX + 12 = i + 100,  centerY - 25 = j + 58
    //
    // The background is blitted 1:1 from UV (0,0), so screen offset == UV coordinate.
    // Stat icons are baked into the background PNG to the left of each text:
    //   icon width = 9, gap ≈ 2 px  →  icon left edge at x = 100 - 9 - 2 = 89  → UV_U = 89
    //   icon top edge is flush with text top  →  UV_V = text_y = 23 / 41 / 58
    private static final int ICON_UV_U   = 89;
    private static final int ICON_SIZE   = 9;
    private static final int HEALTH_UV_V = 23;
    private static final int DAMAGE_UV_V = 41;
    private static final int SPEED_UV_V  = 58;

    // ── State ────────────────────────────────────────────────────────────────────
    private float xMouse;
    private float yMouse;

    /** Cached temporary entity used for the 3-D head rendering. Recreated when the type / skin changes. */
    @Nullable private LivingEntity cachedEntity    = null;
    @Nullable private String       cachedEntityKey = null; // entityType + ":" + skinIndex

    public DNAAnalyserScreen(DNAAnalyserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // Push the "Inventory" label off-screen so it isn't drawn
        this.inventoryLabelY = this.imageHeight + 1;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int i = (this.width  - this.imageWidth)  / 2;
        int j = (this.height - this.imageHeight) / 2;

        // Draw the main background
        g.blit(DNA_ANALYSER_LOCATION, i, j, 0, 0, 176, 166);

        // Check if there is a syringe with valid data
        ItemStack syringeStack = this.menu.getSlot(DNAAnalyserMenu.SYRINGE_SLOT).getItem();
        if (syringeStack.isEmpty()) return;

        SyringeData data = syringeStack.get(OWDataComponentTypes.SYRINGE_DATA.get());
        if (data == null) return;

        renderAnalysisResults(g, i, j, data);
    }

    // ── Analysis results ────────────────────────────────────────────────────────

    private void renderAnalysisResults(GuiGraphics g, int i, int j, SyringeData data) {

        // ── Entity head — top-right ─────────────────────────────────────────────
        updateCachedEntity(data);
        if (cachedEntity != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g,
                    i + 126, j + 4,   // box top-left
                    i + 172, j + 54,  // box bottom-right
                    18,               // scale
                    0.0625F,
                    this.xMouse, this.yMouse,
                    cachedEntity);
        }

        // ── Entity name (coloured + bold) + gender icon ─────────────────────────
        String descId = "entity." + data.entityType().replace(":", ".");
        Component entityName = Component.translatable(descId)
                .withStyle(Style.EMPTY
                        .withColor(TextColor.fromRgb(data.entityColor()))
                        .withBold(true));

        int nameX = i + 30;
        int nameY = j + 20;
        g.drawString(this.font, entityName, nameX, nameY, 0xFFFFFF, false);

        // Gender icon: female = v 36, male = v 48, 12×12
        int genderV = data.isFemale() ? 36 : 48;
        g.blit(MISC_LOCATION, nameX + this.font.width(entityName) + 3, nameY, 0, genderV, 12, 12);

        // ── Stat icons + values ──────────────────────────────────────────────────
        // Icons are blitted from the OW inventory background subregion (see comment above).
        int iconX  = i + 30;
        int textX  = iconX + ICON_SIZE + 2;

        // Health (heart icon from OW inventory background)
        g.blit(OW_INVENTORY_LOCATION, iconX, j + 34, ICON_UV_U, HEALTH_UV_V, ICON_SIZE, ICON_SIZE);
        String healthStr = String.valueOf(Math.round(data.maxHealth() * 2) / 2.0);
        g.drawString(this.font, healthStr, textX, j + 35, 0x8b8b8b, false);

        // Damage (sword icon from OW inventory background)
        g.blit(OW_INVENTORY_LOCATION, iconX, j + 47, ICON_UV_U, DAMAGE_UV_V, ICON_SIZE, ICON_SIZE);
        String damageStr = String.valueOf(Math.round(data.damage() * 10) / 10.0);
        g.drawString(this.font, damageStr, textX, j + 48, 0x8b8b8b, false);

        // Speed (boot/arrow icon from OW inventory background)
        g.blit(OW_INVENTORY_LOCATION, iconX, j + 60, ICON_UV_U, SPEED_UV_V, ICON_SIZE, ICON_SIZE);
        // Convert raw movement speed to blocks/second (multiplier matches OWUtils.getSpeedBlocksPerSecond)
        double speedBps = Math.round(data.speed() * 43.17 * 100) / 100.0;
        g.drawString(this.font, String.valueOf(speedBps), textX, j + 61, 0x8b8b8b, false);

        // Taming experience (dedicated paw PNG, 11×11)
        g.blit(TAMING_ICON_LOCATION, iconX, j + 72, 0, 0, 11, 11, 11, 11);
        String tamingStr = String.valueOf(Math.round(data.tamingExperience() * 10) / 10.0);
        g.drawString(this.font, tamingStr, textX, j + 73, 0x8b8b8b, false);
    }

    // ── Entity caching ───────────────────────────────────────────────────────────

    /**
     * Creates (or reuses) a temporary {@link LivingEntity} for the 3-D entity head preview.
     * The entity is recreated only when the type or skin variant changes.
     * After creation, {@link OWEntity#changeSkin} is called with the saved skin index so the
     * correct visual variant (tiger gold, boss skin, etc.) is displayed.
     */
    private void updateCachedEntity(SyringeData data) {
        String key = data.entityType() + ":" + data.skinIndex();
        if (key.equals(cachedEntityKey)) return;
        cachedEntityKey = key;
        cachedEntity    = null;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ResourceLocation rl = ResourceLocation.tryParse(data.entityType());
        if (rl == null) return;

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(rl).orElse(null);
        if (type == null) return;

        var entity = type.create(mc.level);
        if (!(entity instanceof LivingEntity living)) return;

        // Apply the saved skin variant so the 3-D preview shows the correct appearance
        if (living instanceof OWEntity owEntity && data.skinIndex() != 0) {
            owEntity.changeSkin(data.skinIndex(), false);
        }

        cachedEntity = living;
    }

    // ── Labels ──────────────────────────────────────────────────────────────────

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Centred title at the top; skip the default "Inventory" label
        int titleWidth = this.font.width(this.title);
        g.drawString(this.font, this.title, (this.imageWidth - titleWidth) / 2, 6, 0x404040, false);
    }
}
