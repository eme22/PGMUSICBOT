package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class ClearDataCmd extends AdminCommand {

    public ClearDataCmd(Bot bot)
    {
        this.name = "cleardata";
        this.help = "limpia todos los datos del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(" Configuracion del servidor borrada").queue();
        Settings s = getClient().getSettingsFor(event.getGuild());
        s.clearServerData(event.getGuild());
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        s.clearServerData(event.getGuild());
    }
}
