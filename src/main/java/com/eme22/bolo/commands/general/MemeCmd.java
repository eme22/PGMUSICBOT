package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.MemeImage;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.Collections;
import java.util.Objects;

public class MemeCmd extends SlashCommand {

    public MemeCmd(Bot bot) {
        this.name = "meme";
        this.arguments = "NONE o <posicion>";
        this.help = "muestra un meme al azar del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
        this.options = Collections
                .singletonList(new OptionData(OptionType.INTEGER, "posicion", "posicion del meme").setRequired(false));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Settings s = event.getClient().getSettingsFor(event.getGuild());

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
        StringBuilder builder = new StringBuilder().append(data.getMessage());
        EmbedBuilder eb = new EmbedBuilder().setImage(data.getMeme());
        messageCreateBuilder.setEmbeds(eb.build());
        event.reply(messageCreateBuilder.build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {

        Settings s = event.getClient().getSettingsFor(event.getGuild());

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
        StringBuilder builder = new StringBuilder().append(data.getMessage());
        EmbedBuilder eb = new EmbedBuilder().setImage(data.getMeme());
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.setEmbeds(eb.build());
        event.reply(messageCreateBuilder.build());
        event.getMessage().delete().queue();
    }
}
