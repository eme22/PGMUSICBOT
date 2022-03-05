package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class BotMessageCmd extends AdminCommand {

    public BotMessageCmd(Bot bot) {
        this.name = "message";
        this.help = "hace hablar al bot";
        this.arguments = "<mensaje>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        String image = event.getArgs();
        if(image.isEmpty())
        {
            event.replyError(" Incluya un mensaje");
            return;
        }

        event.getMessage().delete().complete();
        event.getTextChannel().sendMessage(event.getArgs()).complete();
    }
}
