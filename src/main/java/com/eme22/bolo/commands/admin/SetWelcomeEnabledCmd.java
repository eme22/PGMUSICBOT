package com.eme22.bolo.commands.admin;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

import org.springframework.stereotype.Component;

@Component
public class SetWelcomeEnabledCmd extends AdminCommand {

    @Value("${config.aliases.sethelloenabled:}")
    String[] aliases = new String[0];

    public SetWelcomeEnabledCmd(@Qualifier("adminCategory") Category category) {
        super(category);
        this.name = "sethelloon";
        this.help = "activa o desactiva los mensajes de bienvenida";
        this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "estado", "activa o desactiva los mensajes de bienvenida.").setRequired(true));
        this.arguments = "<true - false>";
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        OptionMapping canal = event.getOption("estado");
        Server s = event.getClient().getSettingsFor(event.getGuild());

        if (canal != null && canal.getAsBoolean()) {
            event.reply("El mensaje de bienvenida se ha activado").queue();
            s.setBienvenidasChannelEnabled(true);
        }
        else {
            event.reply("El mensaje de bienvenida se ha desactivado").queue();
            s.setAntiRaidMode(false);
        }

        s.save();

    }

    @Override
    protected void execute(CommandEvent event) {

        String estado = event.getArgs();

        Server s = event.getClient().getSettingsFor(event.getGuild());

        if (estado.equals("true")) {
            event.replySuccess(" El mensaje de bienvenida se ha activado");
            s.setBienvenidasChannelEnabled(true);
            s.save();
        }

        else if (estado.equals("false")) {
            event.replySuccess(" El mensaje de bienvenida se ha desactivado");
            s.setBienvenidasChannelEnabled(false);
            s.save();
        }

        else {
            event.replyError(" Ponga un valor valido");
        }
    }
}
