package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

import org.springframework.stereotype.Component;

@Component
public class DeleteEightBallAnswer extends AdminCommand {

	@Value("${config.aliases.del8ballanswer:}")
	String[] aliases = new String[0];

	public DeleteEightBallAnswer(@Qualifier("adminCategory") Category category) {
		super(category);
		this.name = "del8ballanswer";
		this.arguments = "<answer>";
		this.help = "elimina una respuesta a la bola de 8";
		this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "respuesta que vas a borrar").setRequired(true));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		Server settings = event.getClient().getSettingsFor(event.getGuild());
		Integer answer = event.getOption("posicion", OptionMapping::getAsInt);

		if (answer == null || answer >= settings.getEightBallAnswers().size()) {
			event.reply(event.getClient().getError() + " Posicion incorrecta!!").queue();
			return;
		}

		settings.removeFrom8BallAnswers(answer);
		settings.save();
		event.reply(event.getClient().getSuccess()+ " **Respuesta eliminada en la posicion: " + answer+ " **").queue();
	}

	@Override
	protected void execute(CommandEvent event) {
		Server settings = event.getClient().getSettingsFor(event.getGuild());
		int answer = Integer.parseInt(event.getArgs());

		if (answer >= settings.getEightBallAnswers().size()) {
			event.replyError(" Posicion incorrecta!!");
			return;
		}

		settings.removeFrom8BallAnswers(answer);
		settings.save();
		event.replySuccess(" **Respuesta eliminada en la posicion: " + answer+ " **");
	}
}
