package io.github.tsecho.poketeams.utilities;

import io.github.tsecho.poketeams.enums.messages.ErrorMessage;
import io.github.tsecho.poketeams.enums.messages.QueueMessage;
import io.github.tsecho.poketeams.enums.messages.TechnicalMessage;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ErrorCheck {

    /**
     *
     * @param src to base the messages off of
     * @param problem to send to the player
     * @return a successful command result to end the command
     */
    public static CommandResult test(CommandSource src, ErrorMessage problem) {
        src.sendMessage(problem.getText(src));
        return CommandResult.success();
    }

    /**
     *
     * @param src to base the messages off of
     * @param problem to send to the player
     * @return a successful command result to end the command
     */
    public static CommandResult test(CommandSource src, TechnicalMessage problem) {
        src.sendMessage(problem.getText(src));
        return CommandResult.success();
    }

    /**
     *
     * @param src to base the messages off of
     * @param problem to send to the player
     * @return a successful command result to end the command
     */
    public static CommandResult test(CommandSource src, QueueMessage problem) {
        src.sendMessage(problem.getText(src));
        return CommandResult.success();
    }

    /**
     *
     * @param src to base the messages off of
     * @param problem to send to the player
     * @return a successful command result to end the command
     */
    public static CommandResult test(CommandSource src, String problem) {
        src.sendMessage(Text.builder().append(Text.of(problem)).color(TextColors.RED).build());
        return CommandResult.success();
    }

}
