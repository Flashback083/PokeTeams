package io.github.tsecho.poketeams.eventlisteners;

import com.google.common.collect.ImmutableMap;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import io.github.tsecho.poketeams.PokeTeams;
import io.github.tsecho.poketeams.apis.PokeTeamsAPI;
import io.github.tsecho.poketeams.economy.EconManager;
import io.github.tsecho.poketeams.enums.messages.QueueMessage;
import io.github.tsecho.poketeams.enums.messages.SuccessMessage;
import io.github.tsecho.poketeams.language.Texts;
import io.github.tsecho.poketeams.utilities.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.TypeTokens;

import java.math.BigDecimal;
import java.util.Optional;

import static io.github.tsecho.poketeams.configuration.ConfigManager.getConfNode;

public class PlayerBattleListener {

	@SubscribeEvent
	public void onPlayerBattle(BattleEndEvent e) {

		getParticipants(e.results).ifPresent(participants -> {

			PokeTeamsAPI role = new PokeTeamsAPI(participants[0]);
			PokeTeamsAPI roleOther = new PokeTeamsAPI(participants[1]);

			if(areBattleTeams(role, roleOther) || alwaysRecord()) {
				if(queueAllows(participants[0], participants[1])) {
					sendInternals(participants[0], participants[1], role, roleOther);
					sendOutput(participants[0], participants[1]);
				}
			}
		});
	}

	
	private void sendInternals(Player winner, Player loser, PokeTeamsAPI role, PokeTeamsAPI roleOther) {
		role.addWin(1);
		roleOther.addLoss(1);
		Utils.removeQueue(winner.getName());
		Utils.removeQueue(loser.getName());
	}

	private void sendOutput(Player winner, Player loser) {

		if(getConfNode("Battle-Settings", "Message-Winners").getBoolean())
			winner.sendMessage(QueueMessage.BATTLE_WON.getText(winner));

		if(getConfNode("Battle-Settings", "Message-Losers").getBoolean())
			loser.sendMessage(QueueMessage.BATTLE_LOST.getText(loser));

		if(!getConfNode("Battle-Settings", "Give-Winner-Rewards").getBoolean())
			return;

		try {
			for(String i : getConfNode("Battle-Settings", "Winner-Rewards").getList(TypeTokens.STRING_TOKEN)) {

				if(i.startsWith("[MONEY=")) {

					EconManager econ = new EconManager(winner);

					if(econ.isEnabled()) {

						BigDecimal cost = BigDecimal.valueOf(Integer.valueOf(i.replaceAll("\\D+","")));
						econ.pay(cost);
						winner.sendMessage(Texts.of(SuccessMessage.MONEY_REWARD.getString(winner).replace("%price%", cost.toPlainString()), winner));

					} else {
						PokeTeams.getInstance().getLogger().error("Economy plugin is not available! Please add one for rewards to work properly");
					}

				} else {
					Sponge.getCommandManager().process(Sponge.getServer().getConsole(), i.replace("%player%", winner.getName()));
				}
			}
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
	}

	private boolean queueAllows(Player winner, Player loser) {
		if(getConfNode("Battle-Settings", "Record-Only-Queue").getBoolean())
			return Utils.inQueue(winner.getName()) && Utils.inQueue(loser.getName());
		return true;
	}
	
	private Optional<Player[]> getParticipants(ImmutableMap<BattleParticipant, BattleResults> results) {
		Player[] participants = new Player[2];

		results.forEach((participant, type) -> {
			if(participant instanceof PlayerParticipant) {
                if(type == BattleResults.VICTORY)
                    participants[0] = ((Player) participant.getEntity());
                else if(type == BattleResults.DEFEAT)
                    participants[1] = ((Player) participant.getEntity());
            }
		});

		if(participants[0] != null && participants[1] != null)
        	return Optional.of(participants);
		return Optional.empty();
	}

	private boolean alwaysRecord() {
		return getConfNode("Battle-Settings", "Record-All-Battles").getBoolean();
	}
	
	private boolean areBattleTeams(PokeTeamsAPI role, PokeTeamsAPI roleOther) {
		return (role.inTeam() && roleOther.inTeam()) && (!role.getTeam().equals(roleOther.getTeam()));
	}
}
