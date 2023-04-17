package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
public class CloneChannelCmd extends AdminCommand {

    @Value("${config.aliases.clonechannel:}")
    String[] aliases = new String[0];

    public CloneChannelCmd(@Qualifier("adminCategory") Category category) {
        super(category);
        this.name = "clonechannel";
        this.help = "clona el canal especificado";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        TextChannel channel = event.getTextChannel();
        channel.createCopy().queue(
                success -> {
                    event.reply("El canal se ha clonado con exito").queue();
                },
                error -> {
                    event.reply(event.getClient().getError() + " El canal no se ha podido clonar").queue();
                });
    }

    @Override
    protected void execute(CommandEvent event) {

        TextChannel channel = event.getTextChannel();
        channel.createCopy().queue(
                success -> {
                    event.replySuccess(" El canal se ha clonado con exito!!!");
                },
                error -> {
                    event.replyError("El canal no se ha podido clonar");
                });

    }

}
