package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
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
public class SetGoodByeEnabledCmd extends AdminCommand {

    @Value("${config.aliases.setgoodbyeenabled:}")
    String[] aliases = new String[0];

    public SetGoodByeEnabledCmd(@Qualifier("adminCategory") Category category) {
        super(category);
        this.name = "setgoodbyeon";
        this.help = "activa o desactiva los mensajes de despedida";
        this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "estado", "activa o desactiva los mensajes de despedida.").setRequired(true));
        this.arguments = "<true - false>";
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        OptionMapping canal = event.getOption("estado");
        Server s = event.getClient().getSettingsFor(event.getGuild());

        if (canal != null && canal.getAsBoolean()) {
            s.setDespedidasChannelEnabled(true);
        }
        else
            s.setAntiRaidMode(false);

        s.save();
    }

    @Override
    protected void execute(CommandEvent event) {

        String estado = event.getArgs();

        Server s = event.getClient().getSettingsFor(event.getGuild());

        if (estado.equals("true")) {
            event.replySuccess(" El mensaje de despedida se ha activado");
            s.setDespedidasChannelEnabled(true);
            s.save();
        }

        else if (estado.equals("false")) {
            event.replySuccess(" El mensaje de despedida se ha desactivado");
            s.setDespedidasChannelEnabled(false);
            s.save();
        }

        else {
            event.replyError(" Ponga un valor valido");
        }
    }
}
