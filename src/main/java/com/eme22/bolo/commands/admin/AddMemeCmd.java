package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class AddMemeCmd extends AdminCommand {

    @Value("${config.aliases.addmeme:}")
    String[] aliases = new String[0];

    public AddMemeCmd(@Qualifier("adminCategory") Category category)
    {
        super(category);
        this.name = "addmeme";
        this.help = "agrega un meme para el comando especial de memes, puede ser adjuntado al mensaje";
        this.arguments = "<meme> <link>";
        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "meme", "nombre o descripcion del meme").setRequired(true),
                new OptionData(OptionType.STRING, "link", "link de la imagen del meme").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String message = Objects.requireNonNull(event.getOption("meme")).getAsString();
        String link = Objects.requireNonNull(event.getOption("link")).getAsString();
        try {
            new URL(link);
        } catch (MalformedURLException e) {
            event.reply(event.getClient().getError()+" Link Incorrecto").setEphemeral(true).queue();
            return;
        }
        Server s = event.getClient().getSettingsFor(event.getGuild());
        s.addToMemeImages(message, link);
        s.save();
        event.reply(event.getClient().getSuccess()+" Imagen "+ link +" Agregada a la lista de memes").setEphemeral(true).queue();
    }

    @Override
    protected void execute(CommandEvent event) {

        String message;
        String link = null;

        if(event.getArgs().isEmpty()) {
            List<Message.Attachment> attachmentList = event.getMessage().getAttachments();
            if (attachmentList.isEmpty()) {
                event.reply(event.getClient().getError()+" Incluya texto y un link");
                return;
            }
            else {
                link = attachmentList.get(0).getUrl();
            }
        }

        Server s = event.getClient().getSettingsFor(event.getGuild());

        if (link != null) {
            message = event.getArgs();
        }
        else {
            String args = event.getArgs();
            message = args.substring(0, args.lastIndexOf(" "));
            link = args.substring(args.lastIndexOf(" ")+1);
        }

        try {
            new URL(link);
        } catch (MalformedURLException e) {
            event.replyError(" Link Incorrecto");
            return;
        }
        s.addToMemeImages(message, link);
        s.save();
        event.reply(event.getClient().getSuccess()+" Imagen "+ link +" Agregada a la lista de memes");


    }
}
