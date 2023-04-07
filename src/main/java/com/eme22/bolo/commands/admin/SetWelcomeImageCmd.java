package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.settings.Settings;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

public class SetWelcomeImageCmd extends AdminCommand {

    public SetWelcomeImageCmd(Bot bot)
    {
        this.name = "setwelcomeimg";
        this.help = "cambia la imagen de bienvenidas a una personalizada";
        this.arguments = "<link|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "imagen", "imagen de fondo del mensaje de bienvenidas.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String image = event.getOption("imagen").getAsString();
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(image.equalsIgnoreCase("none"))
        {
            s.setBienvenidasChannelImage(null);
            event.reply(event.getClient().getSuccess()+" La imagen de bienvenidas se ha quitado.").queue();
            return;
        }
        if (OtherUtil.checkImage(image)){
            s.setBienvenidasChannelImage(image);
            event.reply(event.getClient().getSuccess()+"La imagen de bienvenidas es ahora "+image).queue();
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
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(image.equalsIgnoreCase("none"))
        {
            s.setBienvenidasChannelImage(null);
            event.replySuccess(" La imagen de bienvenidas se ha quitado.");
            return;
        }
        if (OtherUtil.checkImage(image)){
            s.setBienvenidasChannelImage(image);
            event.replySuccess(" La imagen de bienvenidas es ahora "+image);
        }
        else {
            event.replyError(" Incluya un link a una imagen valida o NONE para usar la imagen por defecto");
        }
    }

}
