package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.net.MalformedURLException;
import java.net.URL;

public class AddMemeCmd extends AdminCommand {

    public AddMemeCmd(Bot bot)
    {
        this.name = "addmeme";
        this.help = "agrega un meme para el comando especial de memes";
        this.arguments = "<meme> <link>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" Incluya texto y un link");
            return;
        }

        Settings s = event.getClient().getSettingsFor(event.getGuild());

        String args = event.getArgs();
        String message = args.substring(0, args.lastIndexOf(" "));
        String link = args.substring(args.lastIndexOf(" ")+1);
        try {
            new URL(link);
        } catch (MalformedURLException e) {
            event.replyError(event.getClient().getError()+" Link Incorrecto");
            return;
        }
        s.addToMemeImages(message, link);
        event.reply(event.getClient().getSuccess()+" Imagen "+ link +" Agregada a la lista de memes");


    }
}
