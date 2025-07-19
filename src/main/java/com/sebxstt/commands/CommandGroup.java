package com.sebxstt.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import com.sebxstt.functions.commands.FunctionGroup;
import com.sebxstt.functions.utils.Suggest;
import com.sebxstt.providers.PluginProvider;

import java.util.List;

public class CommandGroup {
    public static void build(Commands cmds) {
        cmds.register(Commands.literal("gcreate")
                        .then(Commands.argument("color", StringArgumentType.word())
                                .suggests(Suggest.ColorSuggestions())
                                .then(Commands.argument("nombre", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String name = ctx.getArgument("nombre", String.class);
                                            String colorInput = ctx.getArgument("color", String.class);
                                            FunctionGroup.CreateGroup(ctx, name, colorInput);
                                            return 1;
                                        })
                                )
                        ).build(),
                "Crea un nuevo grupo con un nombre y color",
                List.of("gc")
        );

        cmds.register(Commands.literal("gmenu")
                .executes(ctx -> {
                    FunctionGroup.ShowMenu(ctx);
                    return 1;
                }).build()
        );

        cmds.register(Commands.literal("gchat")
                        .then(Commands.argument("estado", StringArgumentType.word())
                                .suggests(Suggest.OptionsSuggestions(PluginProvider.optionsStates))
                                .executes(ctx -> {
                                    String state = ctx.getArgument("estado", String.class);
                                    FunctionGroup.ChatStateGroup(ctx, state);
                                    return 1;
                                })
                        ).build(),
                "Activa o desactiva el chat de grupo privado",
                List.of("ch")
        );

        cmds.register(Commands.literal("gstorage")
                        .executes(ctx -> {
                            FunctionGroup.ShowStorage(ctx);
                            return 1;
                        }).build(),
                "Abre el almacenamiento compartido del grupo",
                List.of("st")
        );

        cmds.register(Commands.literal("ginfo")
                        .executes(ctx -> {
                            FunctionGroup.PreviewGroup(ctx);
                            return 1;
                        }).build(),
                "Muestra la información actual del grupo",
                List.of("gi")
        );

        cmds.register(Commands.literal("grole")
                        .then(Commands.argument("cargo", StringArgumentType.word())
                                .suggests(Suggest.PlayersTypeSuggestions())
                                .then(Commands.argument("jugador", StringArgumentType.greedyString())
                                        .suggests(Suggest.PlayersSuggestionsTeam())
                                        .executes(ctx -> {
                                            String target = ctx.getArgument("jugador", String.class);
                                            String cargo = ctx.getArgument("cargo", String.class);
                                            FunctionGroup.ChangePostGroup(ctx, target, cargo);
                                            return 1;
                                        })
                                )
                        ).build(),
                "Asigna un rol/cargo a un miembro del grupo",
                List.of("gr")
        );

        cmds.register(Commands.literal("gleave")
                        .executes(ctx -> {
                            FunctionGroup.LeaveGroup(ctx);
                            return 1;
                        }).build(),
                "Abandona el grupo actual",
                List.of("lv")
        );

        cmds.register(Commands.literal("ginvite")
                        .then(Commands.argument("cargo", StringArgumentType.word())
                                .suggests(Suggest.PlayersTypeSuggestions())
                                .then(Commands.argument("jugador", StringArgumentType.greedyString())
                                        .suggests(Suggest.PlayersSuggestions())
                                        .executes(ctx -> {
                                            String target = ctx.getArgument("jugador", String.class);
                                            String cargo = ctx.getArgument("cargo", String.class);
                                            FunctionGroup.InviteGroup(ctx, target, cargo);
                                            return 1;
                                        })
                                )
                        ).build(),
                "Invita a un jugador al grupo con un cargo específico",
                List.of("iv")
        );

        cmds.register(Commands.literal("gkick")
                        .then(Commands.argument("jugador", StringArgumentType.greedyString())
                                .suggests(Suggest.PlayersSuggestionsTeam())
                                .executes(ctx -> {
                                    String target = ctx.getArgument("jugador", String.class);
                                    FunctionGroup.KickGroup(ctx, target);
                                    return 1;
                                })
                        ).build(),
                "Expulsa a un jugador del grupo",
                List.of("gk")
        );

        cmds.register(Commands.literal("gdisband")
                        .executes(ctx -> {
                            FunctionGroup.DisbandGroup(ctx);
                            return 1;
                        }).build(),
                "Disuelve el grupo actual",
                List.of("gd")
        );
    }
}