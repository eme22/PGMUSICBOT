package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class CloneChannelCmd extends AdminCommand {

    public CloneChannelCmd(Bot bot) {
        this.name = "clonechannel";
        this.help = "clona el canal especificado";
        this.aliases = bot.getConfig().getAliases(this.name);
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
                    event.reply("El canal se ha clonado con exito");
                },
                error -> {
                    event.replyError("El canal no se ha podido clonar");
                });

    }

}
