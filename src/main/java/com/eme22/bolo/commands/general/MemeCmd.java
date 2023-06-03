package com.eme22.bolo.commands.general;

import com.eme22.bolo.model.MemeImage;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.stats.StatsService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.eme22.bolo.commands.BaseCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class MemeCmd extends BaseCommand {

    private final StatsService statsService;

    @Autowired
    public MemeCmd(@Value("${config.aliases.meme:}") String[] aliases, StatsService statsService) {
        this.name = "meme";
        this.arguments = "NONE o <posicion>";
        this.help = "muestra un meme al azar del servidor";
        this.aliases = aliases;
        this.statsService = statsService;
        this.guildOnly = true;
        this.options = Collections
                .singletonList(new OptionData(OptionType.INTEGER, "posicion", "posicion del meme").setRequired(false));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Server s = event.getClient().getSettingsFor(event.getGuild());

        Integer pos = null;

        try {
            pos = Integer.parseInt(Objects.requireNonNull(event.getOption("posicion")).getAsString());
        } catch (NullPointerException | NumberFormatException ignore) {
        }

        MemeImage data;
        try {
            if (pos != null)
                data = s.getMemeImages().get(pos - 1);
            else
                data = s.getRandomMemeImages();
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            event.reply(event.getClient().getError() + "Meme invalido o no hay memes configurados en este servidor").queue();
            return;
        }
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        EmbedBuilder eb = new EmbedBuilder().setImage(data.getMeme());
        messageCreateBuilder.addContent(data.getMessage());
        messageCreateBuilder.setEmbeds(eb.build());
        event.reply(messageCreateBuilder.build()).queue(success -> {
            statsService.updateImagesSend(event.getGuild().getIdLong());
            statsService.updateMemesSend(event.getGuild().getIdLong());
        });
    }

    @Override
    protected void execute(CommandEvent event) {

        Server s = event.getClient().getSettingsFor(event.getGuild());

        Integer pos = null;

        try {
            pos = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException ignore) {
        }

        MemeImage data;
        try {
            if (pos != null)
                data = s.getMemeImages().get(pos - 1);
            else
                data = s.getRandomMemeImages();
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            event.replyError("Meme invalido o no hay memes configurados en este servidor");
            return;
        }
        EmbedBuilder eb = new EmbedBuilder().setImage(data.getMeme());
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.addContent(data.getMessage());
        messageCreateBuilder.setEmbeds(eb.build());
        event.reply(messageCreateBuilder.build(), success -> {
            statsService.updateImagesSend(event.getGuild().getIdLong());
            statsService.updateMemesSend(event.getGuild().getIdLong());
        });
        event.getMessage().delete().queue();
    }
}
