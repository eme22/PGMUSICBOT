package com.eme22.bolo.commands.general;

import com.eme22.bolo.commands.BaseCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class PingCmd extends BaseCommand {

    public PingCmd() {
        this.name = "ping";
        this.help = "checks the bot's latency";
        this.guildOnly = false;
        this.aliases = new String[]{"pong"};
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        event.replyFormat("Ping: %dms | Websocket: %dms", event.getHook().getInteraction().getTimeCreated().until(OffsetDateTime.now(), ChronoUnit.MILLIS), event.getJDA().getGatewayPing()).queue();

    }

    protected void execute(CommandEvent event) {
        event.reply("Ping: ...", (m) -> {
            long ping = event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS);
            m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue();
        });
    }

}
