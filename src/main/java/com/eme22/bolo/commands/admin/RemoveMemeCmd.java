package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class RemoveMemeCmd extends AdminCommand {

    @Value("${config.aliases.delmeme:}")
    String[] aliases = new String[0];

    public RemoveMemeCmd(@Qualifier("adminCategory") Category category)
    {
        super(category);
        this.name = "delmeme";
        this.help = "borra un meme de la lista de memes";
        this.arguments = "<posicion>";
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "posicion en la que esta el meme a borrar").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int a = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(event.getOption("posicion")).getAsString()));
        Server s = event.getClient().getSettingsFor(event.getGuild());
        try {
            s.deleteFromMemeImages(a-1);
            s.save();
        } catch (IndexOutOfBoundsException exception) {
            event.reply(event.getClient().getError()+ " Numero incorrecto").setEphemeral(true).queue();
            return;
        }
        event.reply(event.getClient().getSuccess()+" Imagen "+ a +" borrada de la lista de memes").queue();
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
            event.replyError(" Incluya un numero");
            return;
        }

        Server s = event.getClient().getSettingsFor(event.getGuild());
        try {
            s.deleteFromMemeImages(a-1);
            s.save();
        } catch (IndexOutOfBoundsException exception) {
            event.replyError("Numero incorrecto");
            return;
        }

        event.replySuccess(" Imagen "+ a +" borrada de la lista de memes");


    }
}
