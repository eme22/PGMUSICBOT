package com.eme22.bolo.listeners;

import com.eme22.bolo.stats.StatsService;
import com.jagrosh.jdautilities.command.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatsListener  extends ListenerAdapter implements CommandListener {

    private final StatsService statsService;

    @Autowired
    public StatsListener(StatsService statsService) {
        this.statsService = statsService;
    }

    @Override
    public void onCommand(CommandEvent event, Command command) {
        statsService.updateCommandsUsed(event.getGuild().getIdLong());
        CommandListener.super.onCommand(event, command);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommand command) {
        statsService.updateCommandsUsed(event.getGuild().getIdLong());
        CommandListener.super.onSlashCommand(event, command);
    }


}
