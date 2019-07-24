package io.github.tsecho.poketeams.commands.queue;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.battles.rules.clauses.BattleClauseRegistry;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.github.tsecho.poketeams.PokeTeams;
import io.github.tsecho.poketeams.apis.PokeTeamsAPI;
import io.github.tsecho.poketeams.economy.EconManager;
import io.github.tsecho.poketeams.enums.messages.ErrorMessage;
import io.github.tsecho.poketeams.enums.messages.QueueMessage;
import io.github.tsecho.poketeams.enums.messages.TechnicalMessage;
import io.github.tsecho.poketeams.language.Texts;
import io.github.tsecho.poketeams.pixelmon.QueueManager;
import io.github.tsecho.poketeams.utilities.ErrorCheck;
import io.github.tsecho.poketeams.utilities.Permissions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.transaction.ResultType;

import static io.github.tsecho.poketeams.configuration.ConfigManager.getSettings;

public class Join implements CommandExecutor{

	private PokeTeamsAPI role;

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		if(!(src instanceof Player))
			return ErrorCheck.test(src, TechnicalMessage.NOT_PLAYER);

		role = new PokeTeamsAPI(src);

		if(!role.inTeam())
			return ErrorCheck.test(src, ErrorMessage.NOT_IN_TEAM);
		if(!role.canJoinQueue())
			return ErrorCheck.test(src, ErrorMessage.INSUFFICIENT_RANK);
		if(QueueManager.getQueue().contains(src.getName()))
			return ErrorCheck.test (src, QueueMessage.ALREADY_IN_QUEUE);

		PlayerPartyStorage players = Pixelmon.storageManager.getParty(((Player) src).getUniqueId());

		if (!BattleClauseRegistry.getClauseRegistry().getClause("OU_serveur").validateTeam(players.getTeam())) {
			return ErrorCheck.test(src,"Tu ne peux pas rejoindre la queue avec cette \u00E9quipe Pok\u00E9mon !");
		}

		if(getSettings().battle.queueFee.isEnabled) {

			EconManager econ = new EconManager((Player) src);

			if(econ.isEnabled()) {

				if(QueueManager.getQueue().contains(src.getName()))
					return ErrorCheck.test(src, QueueMessage.ALREADY_IN_QUEUE);

				if(econ.enterQueue().getResult() != ResultType.SUCCESS)
					return ErrorCheck.test(src, ErrorMessage.INSUFFICIENT_FUNDS);

				src.sendMessage(Texts.of(QueueMessage.ADDED_QUEUE_COST.getString()
						.replace("%price%", String.valueOf(getSettings().battle.queueFee.price)), src));

				QueueManager.getQueue().add(src.getName());

			} else {
				src.sendMessage(Texts.of("&cEconomy plugin is not installed! Please contact an admin with this messages"));
				PokeTeams.getInstance().getLogger().error("Economy plugin not installed! Economy features will not work correctly!");
			}

		} else {
			QueueManager.getQueue().add(src.getName());
			src.sendMessage(QueueMessage.ADDED_QUEUE.getText(src));
		}
		
		return CommandResult.success();
	}

	public static CommandSpec build() {
		return CommandSpec.builder()
				.permission(Permissions.QUEUE_JOIN)
				.executor(new Join())
				.build();
	}
}