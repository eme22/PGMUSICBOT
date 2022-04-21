package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.MemeImage;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.eme22.bolo.commands.music.QueueCmd.getQueueTitle;

public class MemeListCmd extends SlashCommand {

    private final Paginator.Builder builder;

    public MemeListCmd(Bot bot) {
        this.name = "memelist";
        this.help = "muestra la lista de memes del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
        this.builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignore) {
                    }
                })
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter());
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

        String[] songs = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            songs[i] = (i+1)+": "+data.get(i).getMeme();
        }

        builder.setText((i1, i2) -> "AAA")
                .setItems(songs)
        ;
        builder.build().paginate(event.getChannel(), 1);

        /*
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
         */
    }

}
