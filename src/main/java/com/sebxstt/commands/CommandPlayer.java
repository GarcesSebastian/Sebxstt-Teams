package com.sebxstt.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import com.sebxstt.functions.commands.FunctionPlayer;
import com.sebxstt.functions.utils.Suggest;
import com.sebxstt.functions.utils.ReturnCommand;
import com.sebxstt.providers.PluginProvider;

import java.util.List;

public class CommandPlayer {
    public static void build(Commands cmds) {
        cmds.register(
                Commands.literal("test")
                        .then(Commands.argument("player", StringArgumentType.greedyString())
                                .suggests(Suggest.PlayersSuggestions())
                                .executes(ctx -> {
                                    String name = ctx.getArgument("player", String.class);
                                    FunctionPlayer.test(ctx, name);
                                    return 1;
                                })
                        ).build()
        );

        cmds.register(
                Commands.literal("npc")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String name = ctx.getArgument("name", String.class);
                                    try {
                                        FunctionPlayer.npc(ctx, name);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    return 1;
                                })
                        ).build()
        );

        cmds.register(
                ReturnCommand.create("stats", ctx -> {
                    FunctionPlayer.PreviewPlayer(ctx);
                    return 1;
                }),
                "Ver estadisticas del jugador",
                List.of("est")
        );

        cmds.register(
                ReturnCommand.create("clearteams", ctx -> {
                    FunctionPlayer.ClearTeams(ctx);
                    return 1;
                }),
                "Elimina todos los teams del scoreboard",
                List.of()
        );

        cmds.register(
                ReturnCommand.create("return", ctx -> {
                    FunctionPlayer.ReturnPlayer(ctx);
                    return 1;
                }),
                "Te devuelve al punto de muerte",
                List.of()
        );

        cmds.register(
                Commands.literal("invitations")
                        .then(Commands.argument("option", StringArgumentType.word())
                                .suggests(Suggest.OptionsSuggestions(PluginProvider.optionsInvitations))
                                .then(Commands.argument("equipos", StringArgumentType.greedyString())
                                        .suggests(Suggest.RequestTeamSuggestions())
                                        .executes(ctx -> {
                                            String name = ctx.getArgument("equipos", String.class);
                                            String option  = ctx.getArgument("option", String.class);
                                            FunctionPlayer.RequestPlayer(ctx, name, option);
                                            return 1;
                                        })
                                )
                        )
                        .build(),
                "Invitaciones pendientes a equipos",
                List.of("inv")
        );
    }
}