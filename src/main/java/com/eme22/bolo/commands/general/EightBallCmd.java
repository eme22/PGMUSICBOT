package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

public class EightBallCmd extends SlashCommand {

	public EightBallCmd(Bot bot) {
		this.name = "8ball";
		this.arguments = "<pregunta>";
		this.help = "pregunta al azar a la bola de 8";
		this.aliases = bot.getConfig().getAliases(this.name);
		this.guildOnly = true;
		this.options = Collections
				.singletonList(new OptionData(OptionType.STRING, "pregunta", "pregunta que vas a realizar").setRequired(true));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		Settings settings = getClient().getSettingsFor(event.getGuild());

		String question = event.getOption("pregunta").getAsString();

		EmbedBuilder response = new EmbedBuilder()
				.setTitle("Pregúntale a " + event.getGuild().getSelfMember().getUser().getName()).setDescription(
						"**" + question + "**\n>" + settings.getRandomAnswer());

		event.replyEmbeds(response.build()).queue();
	}

	@Override
	protected void execute(CommandEvent event) {
		Settings settings = event.getClient().getSettingsFor(event.getGuild());

		String question = event.getArgs();

		EmbedBuilder response = new EmbedBuilder()
				.setTitle("Pregúntale a " + event.getGuild().getSelfMember().getUser().getName()).setDescription(
						"**" + question + "**\n>" + settings.getRandomAnswer());

		event.reply(response.build());
	}
}
