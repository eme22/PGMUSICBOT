package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

import org.springframework.stereotype.Component;

@Component
public class AntiRaidModeCmd extends AdminCommand {

    private final Bot bot;

    @Value("${config.aliases.enableantiraidmode:}")
    String[] aliases = new String[0];

    public AntiRaidModeCmd(Bot bot, @Qualifier("adminCategory") Category category) {
        super(category);
        this.bot = bot;
        this.name = "enableantiraidmode";
        this.help = "modo anti raid <on> <off>";
        this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "estado", "activa o desactiva el modo anti raid.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping canal = event.getOption("estado");
        Server s = event.getClient().getSettingsFor(event.getGuild());

        if (canal != null && canal.getAsBoolean()) {
            s.setAntiRaidMode(true);
            makeServerSafe();
        }
        else
            s.setAntiRaidMode(false);

        s.save();

    }

    @Override
    protected void execute(CommandEvent event) {

        String canal = event.getArgs();
        Server s = event.getClient().getSettingsFor(event.getGuild());

        if (canal != null && canal.equals("on")) {
            s.setAntiRaidMode(true);
            s.save();
            makeServerSafe();

        }

        else if (canal != null && canal.equals("off")) {
            s.setAntiRaidMode(false);
            s.save();
        }

    }

    private void makeServerSafe() {
        bot.getJDA().getRoles().forEach(role -> {
            if (role.hasPermission(Permission.MANAGE_CHANNEL))
                role.getManager().revokePermissions(Permission.MANAGE_CHANNEL);
        });
    }


}
