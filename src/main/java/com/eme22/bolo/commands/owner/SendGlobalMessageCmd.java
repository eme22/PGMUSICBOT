package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class SendGlobalMessageCmd extends OwnerCommand {

    public SendGlobalMessageCmd(Bot bot) {
        this.name = "sendom";
        this.help = "sends message from owner to the system channel of every server";
        this.arguments = "<message>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String message = event.getArgs();
        event.getJDA().getGuilds().forEach( guild -> guild.getSystemChannel().sendMessage(message).queue());
    }
}
