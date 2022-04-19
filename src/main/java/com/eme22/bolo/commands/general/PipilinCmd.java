package com.eme22.bolo.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.api.EmbedBuilder;

public class PipilinCmd extends Command {

    public PipilinCmd() {
        this.name = "pipilin";
        this.help = "OwO";
    }

    @Override
    protected void execute(CommandEvent event) {
        // Send a image

        String images[] = {
                "https://media.discordapp.net/attachments/948076380040605726/966047815434399804/unknown.png",
                "https://media.discordapp.net/attachments/948076380040605726/966047829833433108/unknown.png",
                "https://cdn.discordapp.com/attachments/940039489781383198/966047755158044723/IMG_0860.png"
        };

        String pickRandomImage = images[(int) (Math.random() * images.length)];

        EmbedBuilder response = new EmbedBuilder().setImage(pickRandomImage);
        event.getChannel().sendMessageEmbeds(response.build()).queue();

        event.getMessage().delete().queue();
    }
}
