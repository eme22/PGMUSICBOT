package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ReloadSettings extends OwnerCommand {

    private final Bot bot;

    public ReloadSettings(Bot bot) {
        this.bot = bot;
        this.name = "reloadsettings";
        this.help = "reload all settings";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            OtherUtil.loadFileFromGit(new File("serversettings.json"));
            bot.getSettingsManager().loadSettings();
            event.replySuccess( " Se han cargado correctamente las opciones del servidor!");
        } catch (IOException | NoSuchAlgorithmException | NullPointerException e) {
            LoggerFactory.getLogger("Settings")
                    .warn("Se ha fallado en cargar las opciones del servidor, se usaran las locales: " + e);
            event.replyError(" No se han podido cargar las opciones del servidor!");

        }
    }
}
