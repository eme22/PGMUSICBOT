package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.eme22.bolo.commands.BaseCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

import org.springframework.stereotype.Component;

@Component
public class EightBallCmd extends BaseCommand {

	@Value("${config.aliases.8ball:}")
	String[] aliases = new String[0];

	public EightBallCmd(Bot bot) {
		this.name = "8ball";
		this.arguments = "<pregunta>";
		this.help = "pregunta al azar a la bola de 8";
		this.guildOnly = true;
		this.options = Collections
				.singletonList(new OptionData(OptionType.STRING, "pregunta", "pregunta que vas a realizar").setRequired(true));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		Server settings = event.getClient().getSettingsFor(event.getGuild());

		String question = event.getOption("pregunta").getAsString();

		if (question.trim().isEmpty()) {
			event.reply("¡Escribe una pregunta!").complete();
			return;
		}

		EmbedBuilder response = new EmbedBuilder()
				.setTitle("Pregúntale a " + event.getGuild().getSelfMember().getUser().getName()).setDescription(
						"**" + question + "**\n> " + settings.getRandomAnswer());

		event.replyEmbeds(response.build()).queue();
	}

	@Override
	protected void execute(CommandEvent event) {
		Server settings = event.getClient().getSettingsFor(event.getGuild());

		String question = event.getArgs();

		if (question.trim().isEmpty()) {
			event.reply("¡Escribe una pregunta!");
			return;
		}

		EmbedBuilder response = new EmbedBuilder()
				.setTitle("Pregúntale a " + event.getGuild().getSelfMember().getUser().getName()).setDescription(
						"**" + question + "**\n> " + settings.getRandomAnswer());

		event.reply(response.build());
	}
}
