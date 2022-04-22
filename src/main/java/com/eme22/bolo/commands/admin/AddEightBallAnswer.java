package com.eme22.bolo.commands.admin;

import java.util.Collections;

import com.eme22.bolo.Bot;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.SlashCommand;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class AddEightBallAnswer extends SlashCommand {

	public AddEightBallAnswer(Bot bot) {
		this.name = "add8ballanswer";
		this.arguments = "<answer>";
		this.help = "agrega una respuesta a la bola de 8";
		this.aliases = bot.getConfig().getAliases(this.name);
		this.guildOnly = true;
		this.options = Collections
				.singletonList(new OptionData(OptionType.STRING, "respuesta", "respuesta que vas a agregar").setRequired(true));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		Settings settings = getClient().getSettingsFor(event.getGuild());
		String answer = event.getOption("respuesta").getAsString();

		settings.addToEightBallAnswers(answer);
		event.reply("**Respuesta agregada:** " + answer).queue();
	}

}
