package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.MemeImage;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;

import java.util.Map;

public class MemeCmd extends Command {

    public MemeCmd(Bot bot){
        this.name = "meme";
        this.arguments = "NONE o <posicion>";
        this.help = "muestra un meme al azar del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
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
        event.getTextChannel().sendMessage(builder.build()).setEmbeds(eb.build()).complete();
    }
}
