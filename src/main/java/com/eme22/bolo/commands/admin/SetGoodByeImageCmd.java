package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

import org.springframework.stereotype.Component;

@Component
public class SetGoodByeImageCmd extends AdminCommand {

    @Value("${config.aliases.setgoodbyeimage:}")
    String[] aliases = new String[0];

    public SetGoodByeImageCmd(@Qualifier("adminCategory") Category category)
    {
        super(category);
        this.name = "setgoodbyeimg";
        this.help = "cambia la imagen de despedidas a una personalizada";
        this.arguments = "<link|NONE>";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "imagen", "imagen de fondo del mensaje de despedidas.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String image = event.getOption("imagen").getAsString();
        Server s = event.getClient().getSettingsFor(event.getGuild());
        if(image.equalsIgnoreCase("none"))
        {
            s.setDespedidasChannelImage(null);
            s.save();
            event.reply(event.getClient().getSuccess()+" La imagen de despedidas se ha quitado.").queue();
            return;
        }
        if (OtherUtil.checkImage(image)){
            s.setDespedidasChannelImage(image);
            s.save();
            event.reply(event.getClient().getSuccess()+"La imagen de despedidas es ahora "+image).queue();
        }
        else {
            event.reply(event.getClient().getError()+ " Incluya un link a una imagen valida o NONE para usar la imagen por defecto").setEphemeral(true).queue();
        }
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
        Server s = event.getClient().getSettingsFor(event.getGuild());
        if(image.equalsIgnoreCase("none"))
        {
            s.setDespedidasChannelImage(null);
            s.save();
            event.replySuccess(" La imagen de despedidas se ha quitado.");
            return;
        }
        if (OtherUtil.checkImage(image)){
            s.setDespedidasChannelImage(image);
            s.save();
            event.replySuccess(" La imagen de despedidas es ahora "+image);
        }
        else {
            event.replyError(" Incluya un link a una imagen valida o NONE para usar la imagen por defecto");
        }
    }
}
