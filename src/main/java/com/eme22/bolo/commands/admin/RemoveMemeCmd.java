package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.net.MalformedURLException;
import java.net.URL;

public class RemoveMemeCmd extends AdminCommand {

    public RemoveMemeCmd(Bot bot)
    {
        this.name = "delmeme";
        this.help = "borra un meme de la lista de memes";
        this.arguments = "<posicion>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {

        String args = event.getArgs();

        if(args.isEmpty())
        {
            event.reply(event.getClient().getError()+" Incluya un numero");
            return;
        }

        int a;

        try {
            a = Integer.parseInt(args);
        }
        catch (NumberFormatException e){
            event.reply(event.getClient().getError()+" Incluya un numero");
            return;
        }

        Settings s = event.getClient().getSettingsFor(event.getGuild());
        try {
            s.deleteFromMemeImages(a-1);
        } catch (IndexOutOfBoundsException exception) {
            event.replyError("Numero incorrecto");
            return;
        }

        event.reply(event.getClient().getSuccess()+" Imagen "+ a +" borrada de la lista de memes");


    }
}
