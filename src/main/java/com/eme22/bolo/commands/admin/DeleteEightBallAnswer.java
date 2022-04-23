package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

public class DeleteEightBallAnswer extends AdminCommand {

	public DeleteEightBallAnswer(Bot bot) {
		this.name = "del8ballanswer";
		this.arguments = "<answer>";
		this.help = "elimina una respuesta a la bola de 8";
		this.aliases = bot.getConfig().getAliases(this.name);
		this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "respuesta que vas a borrar").setRequired(true));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		Settings settings = getClient().getSettingsFor(event.getGuild());
		int answer = Integer.parseInt(event.getOption("posicion").getAsString());

		if (answer >= settings.getEightBallAnswers().size()) {
			event.reply(getClient().getError() + " Posicion incorrecta!!").queue();
			return;
		}

		settings.removeFrom8BallAnswers(answer);
		event.reply(getClient().getSuccess()+ " **Respuesta eliminada en la posicion: " + answer+ " **").queue();
	}

	@Override
	protected void execute(CommandEvent event) {
		Settings settings = getClient().getSettingsFor(event.getGuild());
		int answer = Integer.parseInt(event.getArgs());

		if (answer >= settings.getEightBallAnswers().size()) {
			event.replyError(" Posicion incorrecta!!");
			return;
		}

		settings.removeFrom8BallAnswers(answer);
		event.replySuccess(" **Respuesta eliminada en la posicion: " + answer+ " **");
	}
}
