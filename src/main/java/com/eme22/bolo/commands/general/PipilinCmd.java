package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class PipilinCmd extends SlashCommand {

    public PipilinCmd()
    {
        this.name = "pipilin";
        this.help = "muestra un pipilin";
    }

    @Override
    protected void execute(SlashCommandEvent event) {


    }

    @Override
    protected void execute(CommandEvent event) {
        super.execute(event);
    }
}
