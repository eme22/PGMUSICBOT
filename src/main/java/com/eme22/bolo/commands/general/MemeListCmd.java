package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.model.MemeImage;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.eme22.bolo.commands.BaseCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class MemeListCmd extends BaseCommand {

    private final Paginator.Builder builder;

    @Value("${config.aliases.memelist:}")
    String[] aliases = new String[0];

    public MemeListCmd(Bot bot) {
        this.name = "memelist";
        this.help = "muestra la lista de memes del servidor";
        this.guildOnly = true;
        this.builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignore) {
                    }
                })
                .setItemsPerPage(20)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(10, TimeUnit.MINUTES);
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        Server s = event.getClient().getSettingsFor(event.getGuild());

        List<MemeImage> data = s.getMemeImages();

        String[] songs = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            songs[i] = data.get(i).getMessage();
        }

        if (songs.length == 0){
            event.reply( event.getClient().getError()+ "No hay memes para mostrar").setEphemeral(true).queue();
            return;
        }

        event.reply(event.getClient().getSuccess()+ " Lista de memes").queue();
        builder.setText("").setItems(songs);
        builder.build().paginate(event.getChannel(), 1);
    }

    @Override
    protected void execute(CommandEvent event) {

        Server s = event.getClient().getSettingsFor(event.getGuild());

        List<MemeImage> data = s.getMemeImages();

        String[] songs = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            songs[i] = data.get(i).getMessage();
        }

        if (songs.length == 0){
            event.replyError(" No hay memes para mostrar");
            return;
        }

        builder.setText(event.getClient().getSuccess()+ " Lista de memes")
                .setItems(songs);
        builder.build().paginate(event.getChannel(), 1);
    }

}
