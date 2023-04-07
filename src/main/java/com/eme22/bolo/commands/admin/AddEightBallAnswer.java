package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

public class AddEightBallAnswer extends AdminCommand {

	public AddEightBallAnswer(Bot bot) {
		this.name = "add8ballanswer";
		this.arguments = "<answer>";
		this.help = "agrega una respuesta a la bola de 8";
		this.aliases = bot.getConfig().getAliases(this.name);
		this.options = Collections.singletonList(new OptionData(OptionType.STRING, "respuesta", "respuesta que vas a agregar").setRequired(true));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		Settings settings = event.getClient().getSettingsFor(event.getGuild());
		String answer = event.getOption("respuesta").getAsString();

		settings.addToEightBallAnswers(answer);
		event.reply(event.getClient().getSuccess()+ " **Respuesta agregada:** " + answer).queue();
	}

	@Override
	protected void execute(CommandEvent event) {
		Settings settings = event.getClient().getSettingsFor(event.getGuild());
		String answer = event.getArgs();

		settings.addToEightBallAnswers(answer);
		event.replySuccess(" **Respuesta agregada:** " + answer);
	}
}
