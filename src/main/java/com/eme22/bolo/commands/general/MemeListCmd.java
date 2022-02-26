package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.Map;

public class MemeListCmd extends Command {

    public MemeListCmd(Bot bot){
        this.name = "memelist";
        this.help = "muestra la lista de memes del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }
    @Override
    protected void execute(CommandEvent event) {

        Settings s = event.getClient().getSettingsFor(event.getGuild());

        List<Map<String, String>> data = s.getMemeImages();

        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        int j = 0;
        for (Map<String, String> datum : data) {
            j++;
            int finalJ = j;
            datum.keySet().forEach(message -> stringBuilder.append(finalJ).append(": ").append(message).append("\n"));

        }

        builder.setDescription(stringBuilder.toString());
        event.getTextChannel().sendMessageEmbeds(builder.build()).complete();
    }
}
