package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

import org.springframework.stereotype.Component;

@Component
public class AddEightBallAnswer extends AdminCommand {

	@Value("${config.aliases.add8ballanswer:}")
	String[] aliases = new String[0];


	public AddEightBallAnswer(@Qualifier("adminCategory") Category category) {
		super(category);
		this.name = "add8ballanswer";
		this.arguments = "<answer>";
		this.help = "agrega una respuesta a la bola de 8";
		this.options = Collections.singletonList(new OptionData(OptionType.STRING, "respuesta", "respuesta que vas a agregar").setRequired(true));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		Server settings = event.getClient().getSettingsFor(event.getGuild());
		String answer = event.getOption("respuesta").getAsString();

		settings.addToEightBallAnswers(answer);
		settings.save();
		event.reply(event.getClient().getSuccess()+ " **Respuesta agregada:** " + answer).queue();
	}

	@Override
	protected void execute(CommandEvent event) {
		Server settings = event.getClient().getSettingsFor(event.getGuild());
		String answer = event.getArgs();

		settings.addToEightBallAnswers(answer);
		settings.save();
		event.replySuccess(" **Respuesta agregada:** " + answer);
	}
}
