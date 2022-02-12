package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;

public class ClearDataCmd extends AdminCommand {

    public ClearDataCmd(Bot bot)
    {
        this.name = "cleardata";
        this.help = "limpia todos los datos del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        s.clearServerData(event.getGuild());
    }
}
