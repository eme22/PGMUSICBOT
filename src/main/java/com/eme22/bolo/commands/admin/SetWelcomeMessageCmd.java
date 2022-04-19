package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;

public class SetWelcomeMessageCmd extends AdminCommand {

    public SetWelcomeMessageCmd(Bot bot) {
        this.name = "setwelcomemsg";
        this.help = "cambia el mensaje de bienvenida";
        this.arguments = "<message>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        String image = event.getArgs();
        if (image.isEmpty()) {
            event.replyError(" Incluya un texto");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());

        s.setBienvenidasChannelMessage(image);
        event.reply(event.getClient().getSuccess() + "El mensaje de bienvenida es ahora: \n" + "\"" + image + "\"");

    }
}