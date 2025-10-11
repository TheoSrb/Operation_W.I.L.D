package net.tiew.operationWild.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;

public class OWCommands {
    public static class AddExperienceCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("addexperience").then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(AddExperienceCommand::execute))
            );
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            int amount = IntegerArgumentType.getInteger(context, "amount");
            try {
                ServerPlayer player = source.getPlayerOrException();
                if (player.getRootVehicle() != null && player.getRootVehicle() != player) {
                    OWEntity.addExperienceCommand((OWEntity) player.getRootVehicle(), amount);
                    source.sendSuccess(() -> Component.translatable("addExperienceCommandWork", amount).setStyle(Style.EMPTY.withColor(0x00FF00)), false);
                } else source.sendSuccess(() -> Component.translatable("addExperienceCommandError").setStyle(Style.EMPTY.withColor(0xFF0000)), false);
            } catch (Exception ignored) {
            }
            return 1;
        }
    }

    public static class SetPrestigeCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("setprestige").then(Commands.argument("amount", IntegerArgumentType.integer(0, 999)).executes(SetPrestigeCommand::execute))
            );
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            int amount = IntegerArgumentType.getInteger(context, "amount");
            try {
                ServerPlayer player = source.getPlayerOrException();
                if (player.getRootVehicle() != null && player.getRootVehicle() != player) {
                    if (player.getRootVehicle() instanceof OWEntity owEntity) owEntity.setPrestigeLevel(amount);
                    source.sendSuccess(() -> Component.translatable("addPrestigeCommandWork", amount).setStyle(Style.EMPTY.withColor(0x00FF00)), false);
                } else source.sendSuccess(() -> Component.translatable("addPrestigeCommandError").setStyle(Style.EMPTY.withColor(0xFF0000)), false);
            } catch (Exception ignored) {
            }
            return 1;
        }
    }

    public static class ForceTameCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("forcetame").executes(ForceTameCommand::execute));
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            try {
                ServerPlayer player = source.getPlayerOrException();

                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle();
                Vec3 endPos = eyePos.add(lookVec.scale(10));

                EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                        player.level(),
                        player,
                        eyePos,
                        endPos,
                        player.getBoundingBox().expandTowards(lookVec.scale(10)).inflate(1.0),
                        entity -> !entity.isSpectator() && entity.isPickable()
                );

                if (hitResult != null && hitResult.getEntity() instanceof OWEntity owEntity && !owEntity.isTame()) {
                    double distance = player.distanceTo(owEntity);
                    if (distance <= 10) {
                        owEntity.setTame(true, player);
                        owEntity.addTamingExperience(owEntity.getTamingExperience() / 2, player);
                        source.sendSuccess(() -> Component.translatable("forceTameCommandSuccess", Component.translatable(String.valueOf("entity.ow." + owEntity.getClass().getSimpleName().toLowerCase().split("entity")[0])))
                                .setStyle(Style.EMPTY.withColor(0x00FF00)), false);
                    } else {
                        source.sendSuccess(() -> Component.translatable("forceTameCommandTooFar")
                                .setStyle(Style.EMPTY.withColor(0xFF0000)), false);
                    }
                } else {
                    source.sendSuccess(() -> Component.translatable("forceTameCommandNoTarget")
                            .setStyle(Style.EMPTY.withColor(0xFF0000)), false);
                }
            } catch (Exception ignored) {
            }
            return 1;
        }
    }
}
