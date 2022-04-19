package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.MemeImage;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;

public class MemeListCmd extends SlashCommand {

    public MemeListCmd(Bot bot) {
        this.name = "memelist";
        this.help = "muestra la lista de memes del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        Settings s = getClient().getSettingsFor(event.getGuild());

        List<MemeImage> data = s.getMemeImages();

        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        int j = 0;
        for (MemeImage datum : data) {
            j++;
            int finalJ = j;
            stringBuilder.append(finalJ).append(": ").append(datum.getMessage()).append("\n");

        }

        builder.setDescription(stringBuilder.toString());
        event.replyEmbeds(builder.build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {

        Settings s = event.getClient().getSettingsFor(event.getGuild());

        List<MemeImage> data = s.getMemeImages();

        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        int j = 0;
        for (MemeImage datum : data) {
            j++;
            int finalJ = j;
            stringBuilder.append(finalJ).append(": ").append(datum.getMessage()).append("\n");

        }

        builder.setDescription(stringBuilder.toString());
        event.getTextChannel().sendMessageEmbeds(builder.build()).queue();
    }
}
