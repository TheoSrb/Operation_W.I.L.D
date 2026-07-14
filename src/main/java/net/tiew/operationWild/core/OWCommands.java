package net.tiew.operationWild.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.packets.to_client.SyncOWTeamPacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamInvites;

public class OWCommands {

    // ── Invitations de tribu : Accepter / Refuser ──────────────────────────────
    public static class TeamInviteAcceptCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(Commands.literal("owteamaccept").executes(TeamInviteAcceptCommand::execute));
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            try {
                ServerPlayer player = context.getSource().getPlayerOrException();
                OWTeamInvites.Invite inv = OWTeamInvites.get(player.getUUID());
                if (inv == null) {
                    reply(player, "owteams.invite.none", 0xFF9944);
                    return 1;
                }
                OWTeam team = inv.team();
                if (team.isMember(player.getUUID())) {
                    OWTeamInvites.consume(player.getUUID());
                    reply(player, "owteams.invite.already_in", 0xFF9944);
                    return 1;
                }
                if (team.getPlayerUUIDs().size() >= team.getMaxPlayers()) {
                    OWTeamInvites.consume(player.getUUID());
                    reply(player, "owteams.invite.full", 0xFF6666);
                    return 1;
                }

                team.getPlayerUUIDs().add(player.getUUID());
                team.getPlayerNames().add(player.getName().getString());
                OWTeamInvites.consume(player.getUUID());

                SyncOWTeamPacket.resyncTeam(context.getSource().getServer(), team);

                reply(player, "owteams.invite.accepted_self", 0x7ddd73, team.getTeamName());

                ServerPlayer inviter = context.getSource().getServer().getPlayerList().getPlayer(inv.inviterUUID());
                if (inviter != null) {
                    inviter.sendSystemMessage(Component.translatable("owteams.invite.accepted_inviter",
                            player.getName().getString()).setStyle(Style.EMPTY.withColor(0x7ddd73)));
                }
            } catch (Exception ignored) {}
            return 1;
        }
    }

    public static class TeamInviteDeclineCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(Commands.literal("owteamdecline").executes(TeamInviteDeclineCommand::execute));
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            try {
                ServerPlayer player = context.getSource().getPlayerOrException();
                OWTeamInvites.Invite inv = OWTeamInvites.consume(player.getUUID());
                if (inv == null) {
                    reply(player, "owteams.invite.none", 0xFF9944);
                    return 1;
                }
                reply(player, "owteams.invite.declined_self", 0xAAAAAA);
                ServerPlayer inviter = context.getSource().getServer().getPlayerList().getPlayer(inv.inviterUUID());
                if (inviter != null) {
                    inviter.sendSystemMessage(Component.translatable("owteams.invite.declined_inviter",
                            player.getName().getString()).setStyle(Style.EMPTY.withColor(0xdd8844)));
                }
            } catch (Exception ignored) {}
            return 1;
        }
    }

    private static void reply(ServerPlayer player, String key, int color, Object... args) {
        player.sendSystemMessage(Component.translatable(key, args).setStyle(Style.EMPTY.withColor(color)));
    }

    public static class AddExperienceCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("addexperience")
                            .then(Commands.argument("player", EntityArgument.player())
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                            .executes(AddExperienceCommand::execute)))
            );
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            int amount = IntegerArgumentType.getInteger(context, "amount");
            try {
                ServerPlayer player = EntityArgument.getPlayer(context, "player");
                if (player.getRootVehicle() != null && player.getRootVehicle() != player) {
                    OWEntity.addExperienceCommand((OWEntity) player.getRootVehicle(), amount);
                    source.sendSuccess(() -> Component.translatable("addExperienceCommandWork", amount)
                            .setStyle(Style.EMPTY.withColor(0x00FF00)), false);
                } else {
                    source.sendSuccess(() -> Component.translatable("addExperienceCommandError")
                            .setStyle(Style.EMPTY.withColor(0xFF0000)), false);
                }
            } catch (Exception ignored) {
            }
            return 1;
        }
    }

    public static class SetPrestigeCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("setprestige")
                            .then(Commands.argument("player", EntityArgument.player())
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(0, 999))
                                            .executes(SetPrestigeCommand::execute)))
            );
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            int amount = IntegerArgumentType.getInteger(context, "amount");
            try {
                ServerPlayer player = EntityArgument.getPlayer(context, "player");
                // "prestige" est désormais la monnaie "Pièces Sauvages" du joueur (porte-monnaie partagé).
                OWCurrency.setWildCoins(player, amount);
                OWCurrency.syncWildCoins(player);
                source.sendSuccess(() -> Component.translatable("addPrestigeCommandWork", amount)
                        .setStyle(Style.EMPTY.withColor(0x00FF00)), false);
            } catch (Exception ignored) {
            }
            return 1;
        }
    }

    public static class ForceTameCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("forcetame")
                            .then(Commands.argument("player", EntityArgument.player())
                                    .executes(ForceTameCommand::execute))
            );
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            try {
                ServerPlayer player = EntityArgument.getPlayer(context, "player");

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
                        source.sendSuccess(() -> Component.translatable("forceTameCommandSuccess",
                                        Component.translatable(String.valueOf("entity.ow." + owEntity.getClass().getSimpleName().toLowerCase().split("entity")[0])))
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

    public static class GetTamingExperienceCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("tamingexperience").executes(GetTamingExperienceCommand::execute)
            );
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            try {
                ServerPlayer player = source.getPlayerOrException();
                player.sendSystemMessage(Component.translatable(String.valueOf(OWTamingXp.getTamingXp(player))));
            } catch (Exception ignored) {
            }
            return 1;
        }
    }

    public static class GameRuleNoEffortCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                    Commands.literal("gamerule")
                            .then(Commands.literal("OWAnimalsNoEffort")
                                    .then(Commands.argument("value", BoolArgumentType.bool())
                                            .executes(GameRuleNoEffortCommand::execute))
                                    .executes(GameRuleNoEffortCommand::query))
            );
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            boolean value = BoolArgumentType.getBool(context, "value");
            try {
                source.getLevel().getGameRules()
                        .getRule(OWGameRules.ANIMALS_NO_EFFORT)
                        .set(value, source.getServer());
                source.sendSuccess(() -> Component.translatable("gamerule.OWAnimalsNoEffort.set", value)
                        .setStyle(Style.EMPTY.withColor(0x00FF00)), true);
            } catch (Exception ignored) {}
            return 1;
        }

        private static int query(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            try {
                boolean value = source.getLevel().getGameRules()
                        .getBoolean(OWGameRules.ANIMALS_NO_EFFORT);
                source.sendSuccess(() -> Component.translatable("gamerule.OWAnimalsNoEffort.query", value)
                        .setStyle(Style.EMPTY.withColor(0xFFFFFF)), false);
            } catch (Exception ignored) {}
            return 1;
        }
    }
}