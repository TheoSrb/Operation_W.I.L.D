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

    // ── Debug : peupler sa tribu de faux membres pour tester les permissions ──
    public static class TribeTestMembersCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(Commands.literal("owtribetestmembers").executes(TribeTestMembersCommand::add));
            dispatcher.register(Commands.literal("owtriberemovetestmembers").executes(TribeTestMembersCommand::remove));
        }

        private static final String[] FAKE = { "DevAdjoint", "DevMembre1", "DevMembre2" };

        private static java.util.UUID fakeUuid(String name) {
            return java.util.UUID.nameUUIDFromBytes(("owfake:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        private static int add(CommandContext<CommandSourceStack> context) {
            try {
                ServerPlayer player = context.getSource().getPlayerOrException();
                net.minecraft.server.MinecraftServer server = context.getSource().getServer();
                net.tiew.operationWild.team.OWTribesSavedData data =
                        net.tiew.operationWild.team.OWTribesSavedData.get(server);
                net.tiew.operationWild.team.OWTeam team = data.findTeamByMember(player.getUUID());
                if (team == null) {
                    player.sendSystemMessage(Component.literal("Tu n'as pas de tribu (touche T pour en créer une).")
                            .setStyle(Style.EMPTY.withColor(0xFF9944)));
                    return 1;
                }
                for (int i = 0; i < FAKE.length; i++) {
                    java.util.UUID u = fakeUuid(FAKE[i]);
                    if (!team.isMember(u)) team.addPlayerMember(u, FAKE[i]);
                    team.setDeputy(u, i == 0); // le premier = chef adjoint
                    team.setPermissions(u, i == 0
                            ? net.tiew.operationWild.team.OWTribePermission.DEPUTY_DEFAULT
                            : net.tiew.operationWild.team.OWTribePermission.MEMBER_DEFAULT);
                }
                data.putTribe(team);
                net.tiew.operationWild.team.OWTribeManager.syncTribeToOnlineMembers(server, team);
                net.tiew.operationWild.team.OWTribeManager.broadcastTribeList(server);
                player.sendSystemMessage(Component.literal("3 membres de test ajoutés (1 adjoint + 2 membres).")
                        .setStyle(Style.EMPTY.withColor(0x7ddd73)));
            } catch (Exception ignored) {}
            return 1;
        }

        private static int remove(CommandContext<CommandSourceStack> context) {
            try {
                ServerPlayer player = context.getSource().getPlayerOrException();
                net.minecraft.server.MinecraftServer server = context.getSource().getServer();
                net.tiew.operationWild.team.OWTribesSavedData data =
                        net.tiew.operationWild.team.OWTribesSavedData.get(server);
                net.tiew.operationWild.team.OWTeam team = data.findTeamByMember(player.getUUID());
                if (team == null) return 1;
                for (String name : FAKE) team.removePlayerMember(fakeUuid(name));
                data.putTribe(team);
                net.tiew.operationWild.team.OWTribeManager.syncTribeToOnlineMembers(server, team);
                net.tiew.operationWild.team.OWTribeManager.broadcastTribeList(server);
                player.sendSystemMessage(Component.literal("Membres de test retirés.")
                        .setStyle(Style.EMPTY.withColor(0xAAAAAA)));
            } catch (Exception ignored) {}
            return 1;
        }
    }

    // ── Invitations player-centric (refonte) : /owtribeaccept /owtribedecline ──
    public static class TribeInviteAcceptCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(Commands.literal("owtribeaccept").executes(TribeInviteAcceptCommand::execute));
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            try {
                ServerPlayer player = context.getSource().getPlayerOrException();
                net.minecraft.server.MinecraftServer server = context.getSource().getServer();
                net.tiew.operationWild.team.OWTribeInvites.Invite inv =
                        net.tiew.operationWild.team.OWTribeInvites.get(player.getUUID());
                if (inv == null) { reply(player, "owteams.invite.none", 0xFF9944); return 1; }

                net.tiew.operationWild.team.OWTribesSavedData data =
                        net.tiew.operationWild.team.OWTribesSavedData.get(server);
                if (data.findTeamByMember(player.getUUID()) != null) {
                    net.tiew.operationWild.team.OWTribeInvites.consume(player.getUUID());
                    reply(player, "owteams.invite.already_in", 0xFF9944);
                    return 1;
                }
                OWTeam team = data.findTeamById(inv.teamId());
                if (team == null) {
                    net.tiew.operationWild.team.OWTribeInvites.consume(player.getUUID());
                    reply(player, "owteams.invite.none", 0xFF9944);
                    return 1;
                }
                if (team.getPlayerUUIDs().size() >= team.getMaxPlayers()) {
                    net.tiew.operationWild.team.OWTribeInvites.consume(player.getUUID());
                    reply(player, "owteams.invite.full", 0xFF6666);
                    return 1;
                }

                team.addPlayerMember(player.getUUID(), player.getName().getString());
                team.setPermissions(player.getUUID(), net.tiew.operationWild.team.OWTribePermission.MEMBER_DEFAULT);
                data.putTribe(team);
                net.tiew.operationWild.team.OWTribeInvites.consume(player.getUUID());

                net.tiew.operationWild.team.OWTribeManager.refreshEntitiesOfPlayer(server, player.getUUID());
                net.tiew.operationWild.team.OWTribeManager.syncTribeToOnlineMembers(server, team);
                net.tiew.operationWild.team.OWTribeManager.broadcastTribeList(server);

                reply(player, "owteams.invite.accepted_self", 0x7ddd73, team.getTeamName());
                ServerPlayer inviter = server.getPlayerList().getPlayer(inv.inviterUUID());
                if (inviter != null) {
                    inviter.sendSystemMessage(Component.translatable("owteams.invite.accepted_inviter",
                            player.getName().getString()).setStyle(Style.EMPTY.withColor(0x7ddd73)));
                }
            } catch (Exception ignored) {}
            return 1;
        }
    }

    public static class TribeInviteDeclineCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(Commands.literal("owtribedecline").executes(TribeInviteDeclineCommand::execute));
        }

        private static int execute(CommandContext<CommandSourceStack> context) {
            try {
                ServerPlayer player = context.getSource().getPlayerOrException();
                net.tiew.operationWild.team.OWTribeInvites.Invite inv =
                        net.tiew.operationWild.team.OWTribeInvites.consume(player.getUUID());
                if (inv == null) { reply(player, "owteams.invite.none", 0xFF9944); return 1; }
                reply(player, "owteams.invite.declined_self", 0xAAAAAA);
                net.minecraft.server.MinecraftServer server = context.getSource().getServer();
                ServerPlayer inviter = server.getPlayerList().getPlayer(inv.inviterUUID());
                if (inviter != null) {
                    inviter.sendSystemMessage(Component.translatable("owteams.invite.declined_inviter",
                            player.getName().getString()).setStyle(Style.EMPTY.withColor(0xdd8844)));
                }
            } catch (Exception ignored) {}
            return 1;
        }
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