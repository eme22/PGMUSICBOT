package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.settings.Settings;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SetGoodByeImageCmd extends AdminCommand {

    public SetGoodByeImageCmd(Bot bot)
    {
        this.name = "setgoodbyeimg";
        this.help = "cambia la imagen de despedidas a una personalizada";
        this.arguments = "<link|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    @Override
    protected void execute(CommandEvent event)
    {
        String image = event.getArgs();
        if(image.isEmpty())
        {
            event.replyError(" Incluya un link a una imagen o NONE para usar la imagen por defecto");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(image.equalsIgnoreCase("none"))
        {
            s.setGoodByeImage(null);
            event.reply(event.getClient().getSuccess()+" La imagen de despedidas se ha quitado.");
            return;
        }
        if (OtherUtil.checkImage(image)){
            s.setGoodByeImage(image);
            event.reply(event.getClient().getSuccess()+"La imagen de despedidas es ahora "+image);
        }
        else {
            event.replyError(" Incluya un link a una imagen valida o NONE para usar la imagen por defecto");
        }
    }
}
