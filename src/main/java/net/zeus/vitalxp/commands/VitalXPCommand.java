package net.zeus.vitalxp.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.zeus.vitalxp.Config;

import java.util.function.Supplier;

public class VitalXPCommand {
    public static final SuggestionProvider<CommandSourceStack> PARAM_SUGGESTIONS = (context, builder) -> {
        return net.minecraft.commands.SharedSuggestionProvider.suggest(
                new String[]{"minHealth", "maxHealth", "levelsPerHeart"},
                builder
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("vitalxp")
                        //.requires(source -> source.hasPermission(2))
                        .then(Commands.literal("help")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();

                                    if (source.getEntity() instanceof ServerPlayer player) {

                                        player.sendSystemMessage(Component.literal("")
                                                // title
                                                .append(Component.literal("=== VITAL XP COMMANDS ===\n")
                                                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))

                                                // help
                                                .append(Component.literal("\n/vitalxp help\n")
                                                        .withStyle(ChatFormatting.YELLOW))
                                                .append(Component.literal("  Show this help menu\n")
                                                        .withStyle(ChatFormatting.GRAY))

                                                // set
                                                .append(Component.literal("\n/vitalxp set <param> <value>\n")
                                                        .withStyle(ChatFormatting.YELLOW))
                                                .append(Component.literal("  Change a VitalXP configuration value\n")
                                                        .withStyle(ChatFormatting.GRAY))

                                                // params
                                                .append(Component.literal("\nAvailable parameters:\n")
                                                        .withStyle(ChatFormatting.AQUA))

                                                .append(Component.literal(" • minHealth\n")
                                                        .withStyle(ChatFormatting.WHITE))
                                                .append(Component.literal("   Minimum health at level 0 (half-hearts ×2)\n")
                                                        .withStyle(ChatFormatting.DARK_GRAY))

                                                .append(Component.literal(" • maxHealth\n")
                                                        .withStyle(ChatFormatting.WHITE))
                                                .append(Component.literal("   Maximum possible health (half-hearts ×2)\n")
                                                        .withStyle(ChatFormatting.DARK_GRAY))

                                                .append(Component.literal(" • levelsPerHeart\n")
                                                        .withStyle(ChatFormatting.WHITE))
                                                .append(Component.literal("   XP levels required to gain one heart\n")
                                                        .withStyle(ChatFormatting.DARK_GRAY))

                                                // locked hearts
                                                .append(Component.literal("\n/vitalxp lockedHearts <true | false>\n")
                                                        .withStyle(ChatFormatting.YELLOW))
                                                .append(Component.literal("  Enable or disable locked hearts system\n")
                                                        .withStyle(ChatFormatting.GRAY))
                                        );
                                    }
                                    return 1;
                                })
                        )

                        .then(Commands.literal("set")
                                .then(Commands.argument("param", StringArgumentType.word())
                                        .suggests(PARAM_SUGGESTIONS)
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    String param = StringArgumentType.getString(ctx, "param");
                                                    double value = DoubleArgumentType.getDouble(ctx, "value");

                                                    switch (param.toLowerCase()) {
                                                        case "minhealth" -> {
                                                            if (value > Config.maxHealth) {
                                                                ctx.getSource().sendFailure(Component.literal("Can't set min health higher than max health"));
                                                                return 0;
                                                            }

                                                            if (value < 1) {
                                                                ctx.getSource().sendFailure(Component.literal("Can't set min health lower than 1"));
                                                                return 0;
                                                            }

                                                            Config.minHealth = value;
                                                        }
                                                        case "maxhealth" -> {
                                                            if (value < Config.minHealth) {
                                                                ctx.getSource().sendFailure(Component.literal("Can't set max health lower than min health"));
                                                                return 0;
                                                            }

                                                            if (value > 200) {
                                                                ctx.getSource().sendFailure(Component.literal("Can't set max health higher than 200"));
                                                                return 0;
                                                            }

                                                            Config.maxHealth = value;
                                                        }
                                                        case "levelsperheart" -> Config.levelsPerHeart = (int) value;
                                                        default -> {
                                                            ctx.getSource().sendFailure(Component.literal("Unknown parameter: " + param));
                                                            return 0;
                                                        }
                                                    }

                                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                                        player.sendSystemMessage(Component.literal(param + " has been set to " + value));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(Commands.literal("lockedHearts")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                            Config.lockedHeartsEnabled = enabled;

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Locked hearts " + (enabled ? "enabled" : "disabled")).withStyle(ChatFormatting.GREEN),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
        );
    }
}
