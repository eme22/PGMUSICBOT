package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.eme22.bolo.commands.BaseCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
public class RemoveBirthdayCmd extends BaseCommand {

    Bot bot;

    @Value("${config.aliases.removebirthday:}")
    String[] aliases = new String[0];

    public RemoveBirthdayCmd(Bot bot) {
        this.bot = bot;
        this.name = "removebirthday";
        this.help = "borra tu cumpleaños del servidor";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        Server s = bot.getSettingsManager().getSettings(event.getGuild());

            s.removeBirthDay(event.getUser().getIdLong());

            s.save();

            event.reply(event.getClient().getSuccess()+ " Se ha borrado tu cumpleaños").setEphemeral(true).queue();

    }

    @Override
    protected void execute(CommandEvent event) {

        Server s = bot.getSettingsManager().getSettings(event.getGuild());

        s.removeBirthDay(event.getMember().getUser().getIdLong());

        s.save();

        event.replySuccess(" Se ha borrado tu cumpleaños");


    }
}
