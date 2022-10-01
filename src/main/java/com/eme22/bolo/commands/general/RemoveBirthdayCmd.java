package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.Birthday;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.Calendar;

public class RemoveBirthdayCmd extends SlashCommand {

    Bot bot;

    public RemoveBirthdayCmd(Bot bot) {
        this.bot = bot;
        this.name = "removebirthday";
        this.help = "borra tu cumpleaños del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

            bot.getSettingsManager().getSettings(event.getGuild()).removeBirthDay(event.getUser().getIdLong());

            event.reply(getClient().getSuccess()+ " Se ha borrado tu cumpleaños").setEphemeral(true).queue();


    }

    @Override
    protected void execute(CommandEvent event) {

        bot.getSettingsManager().getSettings(event.getGuild()).removeBirthDay(event.getMember().getUser().getIdLong());

        event.replySuccess(" Se ha borrado tu cumpleaños");


    }
}
