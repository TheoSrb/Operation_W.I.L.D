package net.tiew.operationWild.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.entity.misc.TranquilizerDart;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.projectile.TranquilizerDartItem;
import net.tiew.operationWild.sound.OWSounds;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class SyringeRifleItem extends ProjectileWeaponItem {
    public static final Predicate<ItemStack> DARTS = stack -> stack.getItem() instanceof TranquilizerDartItem;

    public static final int RELOAD_DURATION = 40;
    public static final int LOAD_INSERT_TICK = 17;
    public static final int LOAD_SNAP_TICK = 32;
    public static final int RECOIL_TICKS = 8;
    public static final int AIM_DURATION = 10;
    public static final float SHOT_VELOCITY = 3.4f;
    public static final float HIP_INACCURACY = 3.0f;
    public static final float AIMED_INACCURACY = 0.5f;
    public static final float SCOPED_INACCURACY = 0.0f;
    public static final int SWITCH_COOLDOWN = 20;
    public static final double RELOAD_SLOWDOWN = -0.5;
    private static final ResourceLocation RELOAD_SLOWDOWN_ID = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "syringe_rifle_reload_slowdown");
    private static final ResourceLocation RELOAD_NO_JUMP_ID = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "syringe_rifle_reload_no_jump");
    private static final double RELOAD_NO_JUMP = -1.0;
    private static final Map<Player, WeakReference<ItemStack>> LAST_ACTIVE = new WeakHashMap<>();

    private final boolean scoped;

    public SyringeRifleItem(Properties properties, boolean scoped) {
        super(properties);
        this.scoped = scoped;
    }

    public boolean isScoped() {
        return this.scoped;
    }

    public static boolean isLoaded(ItemStack stack) {
        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        return charged != null && !charged.isEmpty();
    }

    public static boolean isReloading(ItemStack stack) {
        return stack.getItem() instanceof SyringeRifleItem && stack.has(OWDataComponentTypes.SYRINGE_RIFLE_RELOAD_END.get());
    }

    public static float getReloadProgress(ItemStack stack, Level level, float partialTick) {
        Long end = stack.get(OWDataComponentTypes.SYRINGE_RIFLE_RELOAD_END.get());
        if (end == null) return 0f;
        float remaining = end - level.getGameTime() - partialTick;
        return Mth.clamp(1f - remaining / RELOAD_DURATION, 0f, 1f);
    }

    public static float getRecoil(ItemStack stack, Level level, float partialTick) {
        Long lastShot = stack.get(OWDataComponentTypes.SYRINGE_RIFLE_LAST_SHOT.get());
        if (lastShot == null) return 0f;
        float elapsed = level.getGameTime() - lastShot + partialTick;
        if (elapsed < 0f || elapsed >= RECOIL_TICKS) return 0f;
        if (elapsed < 1.5f) return elapsed / 1.5f;
        float decay = 1f - (elapsed - 1.5f) / (RECOIL_TICKS - 1.5f);
        return decay * decay;
    }

    public static boolean isAiming(LivingEntity entity) {
        ItemStack stack = entity.getUseItem();
        return entity.isUsingItem() && stack.getItem() instanceof SyringeRifleItem && isLoaded(stack);
    }

    public static ItemStack activeRifle(LivingEntity entity) {
        if (entity.getMainHandItem().getItem() instanceof SyringeRifleItem) return entity.getMainHandItem();
        if (entity.getOffhandItem().getItem() instanceof SyringeRifleItem) return entity.getOffhandItem();
        return ItemStack.EMPTY;
    }

    public static void tickReloadSlowdown(Player player) {
        boolean reloading = isReloading(activeRifle(player));
        applyReloadModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), RELOAD_SLOWDOWN_ID, RELOAD_SLOWDOWN, reloading);
        applyReloadModifier(player.getAttribute(Attributes.JUMP_STRENGTH), RELOAD_NO_JUMP_ID, RELOAD_NO_JUMP, reloading);
    }

    private static void applyReloadModifier(AttributeInstance attribute, ResourceLocation id, double amount, boolean active) {
        if (attribute == null) return;
        boolean present = attribute.hasModifier(id);
        if (active && !present) {
            attribute.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (!active && present) {
            attribute.removeModifier(id);
        }
    }

    private static void trackSwitch(Player player, ItemStack active) {
        WeakReference<ItemStack> last = LAST_ACTIVE.get(player);
        ItemStack previous = last != null ? last.get() : null;
        if (previous == active) return;
        LAST_ACTIVE.put(player, new WeakReference<>(active));
        if (previous == null) return;
        player.getCooldowns().addCooldown(OWItems.SYRINGE_RIFLE.get(), SWITCH_COOLDOWN);
        player.getCooldowns().addCooldown(OWItems.SYRINGE_RIFLE_SPY_GLASS.get(), SWITCH_COOLDOWN);
    }

    public static boolean isScoping(LivingEntity entity) {
        return isAiming(entity) && entity.getUseItem().getItem() instanceof SyringeRifleItem rifle && rifle.isScoped();
    }

    public static float getAimProgress(LivingEntity entity, float partialTick) {
        if (!isAiming(entity)) return 0f;
        return Math.min((entity.getTicksUsingItem() + partialTick) / AIM_DURATION, 1f);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isLoaded(stack) || activeRifle(player) != stack) return InteractionResultHolder.fail(stack);

        if (this.scoped) player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!isLoaded(stack) || activeRifle(entity) != stack) return;

        if (this.scoped) entity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
        stack.set(OWDataComponentTypes.SYRINGE_RIFLE_LAST_SHOT.get(), level.getGameTime());
        if (level instanceof ServerLevel serverLevel) this.fire(serverLevel, entity, stack, this.getUseDuration(stack, entity) - timeLeft);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) return;

        Long end = stack.get(OWDataComponentTypes.SYRINGE_RIFLE_RELOAD_END.get());
        boolean held = activeRifle(player) == stack;
        if (held) trackSwitch(player, stack);
        if (!held || isLoaded(stack)) {
            if (end != null) stack.remove(OWDataComponentTypes.SYRINGE_RIFLE_RELOAD_END.get());
            return;
        }

        long now = level.getGameTime();
        if (end == null) {
            Long lastShot = stack.get(OWDataComponentTypes.SYRINGE_RIFLE_LAST_SHOT.get());
            if (lastShot != null && now - lastShot < RECOIL_TICKS) return;
            if (player.getProjectile(stack).isEmpty()) return;
            stack.set(OWDataComponentTypes.SYRINGE_RIFLE_RELOAD_END.get(), now + RELOAD_DURATION);
            player.getCooldowns().addCooldown(this, RELOAD_DURATION);
            playSound(level, player, OWSounds.SYRINGE_RIFLE_LOAD_START.get(), 1.0F);
            return;
        }

        long elapsed = RELOAD_DURATION - (end - now);
        if (elapsed == LOAD_INSERT_TICK) {
            playSound(level, player, OWSounds.SYRINGE_RIFLE_LOAD_MIDDLE.get(), 1.0F);
        } else if (elapsed == LOAD_SNAP_TICK) {
            playSound(level, player, OWSounds.SYRINGE_RIFLE_LOADED.get(), 1.0F);
        }

        if (now >= end) {
            stack.remove(OWDataComponentTypes.SYRINGE_RIFLE_RELOAD_END.get());
            this.tryLoad(stack, player);
        }
    }

    private void tryLoad(ItemStack rifle, Player player) {
        ItemStack ammo = player.getProjectile(rifle);
        if (ammo.isEmpty()) return;

        List<ItemStack> drawn = draw(rifle, ammo, player);
        if (drawn.isEmpty()) return;

        rifle.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(drawn.get(0)));
    }

    private void fire(ServerLevel level, LivingEntity shooter, ItemStack rifle, int aimTicks) {
        ItemStack ammo = rifle.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).getItems().get(0);
        rifle.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);

        boolean concentrated = ammo.getItem() instanceof TranquilizerDartItem dart && dart.isConcentrated();
        TranquilizerDart projectile = new TranquilizerDart(shooter, level, ammo, rifle, concentrated);

        float steadiness = Math.min((float) aimTicks / AIM_DURATION, 1f);
        float inaccuracy = Mth.lerp(steadiness, HIP_INACCURACY, this.scoped ? SCOPED_INACCURACY : AIMED_INACCURACY);
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, SHOT_VELOCITY, inaccuracy);
        level.addFreshEntity(projectile);

        HumanoidArm arm = shooter.getUsedItemHand() == InteractionHand.MAIN_HAND ? shooter.getMainArm() : shooter.getMainArm().getOpposite();
        spawnMuzzleSmoke(level, shooter, arm);

        rifle.hurtAndBreak(1, shooter, LivingEntity.getSlotForHand(shooter.getUsedItemHand()));
        playSound(level, shooter, OWSounds.SYRINGE_RIFLE_SHOOT.get(), Mth.randomBetween(level.random, 0.95F, 1.05F));
        playSound(level, shooter, OWSounds.SYRINGE_RIFLE_CRACK.get(), Mth.randomBetween(level.random, 0.95F, 1.05F));
        playSound(level, shooter, OWSounds.SYRINGE_RIFLE_TRIGGER.get(), 1.0F);
        if (shooter instanceof Player player) player.awardStat(Stats.ITEM_USED.get(this));
    }

    private static void spawnMuzzleSmoke(ServerLevel level, LivingEntity shooter, HumanoidArm arm) {
        Vec3 look = shooter.getLookAngle();
        float yaw = shooter.getYRot() * Mth.DEG_TO_RAD;
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        Vec3 muzzle = shooter.getEyePosition()
                .add(look.scale(1.1))
                .add(-Mth.cos(yaw) * 0.22 * side, -0.15, -Mth.sin(yaw) * 0.22 * side);
        level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 4, 0.03, 0.03, 0.03, 0.01);
        level.sendParticles(ParticleTypes.POOF, muzzle.x, muzzle.y, muzzle.z, 1, 0.0, 0.0, 0.0, 0.01);
    }

    private static void playSound(Level level, LivingEntity entity, SoundEvent sound, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, 1.0F, pitch);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return DARTS;
    }

    @Override
    public ItemStack getDefaultCreativeAmmo(@Nullable Player player, ItemStack projectileWeaponItem) {
        return new ItemStack(OWItems.TRANQUILIZER_DART.get());
    }

    @Override
    public int getDefaultProjectileRange() {
        return 30;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (charged != null && !charged.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.ow.syringe_rifle.loaded", charged.getItems().get(0).getHoverName()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.ow.syringe_rifle.empty").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
