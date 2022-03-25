package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.MemeImage;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class MemeCmd extends SlashCommand {

    public MemeCmd(Bot bot){
        this.name = "meme";
        this.arguments = "NONE o <posicion>";
        this.help = "muestra un meme al azar del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "posicion del meme").setRequired(false));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Settings s = getClient().getSettingsFor(event.getGuild());

        Integer pos = null;

        try {
            pos = Integer.parseInt(Objects.requireNonNull(event.getOption("posicion")).getAsString());
        } catch (NullPointerException | NumberFormatException ignore) {}

        MemeImage data;
        try {
            if (pos != null)
                data = s.getMemeImages().get(pos-1);
            else
                data = s.getRandomMemeImages();
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            event.reply(getClient().getError() + "Meme invalido o no hay memes configurados en este servidor").queue();
            return;
        }
        MessageBuilder builder = new MessageBuilder().append(data.getMessage());
        EmbedBuilder eb = new EmbedBuilder().setImage(data.getMeme());
        builder.setEmbeds(eb.build());
        event.reply(builder.build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {

        Settings s = event.getClient().getSettingsFor(event.getGuild());

        Integer pos = null;

        try {
            pos = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException ignore) {}

        MemeImage data;
        try {
            if (pos != null)
                data = s.getMemeImages().get(pos-1);
            else
                data = s.getRandomMemeImages();
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            event.replyError("Meme invalido o no hay memes configurados en este servidor");
            return;
        }
        MessageBuilder builder = new MessageBuilder().append(data.getMessage());
        EmbedBuilder eb = new EmbedBuilder().setImage(data.getMeme());
        builder.setEmbeds(eb.build());
        event.getTextChannel().sendMessage(builder.build()).complete();
    }
}
