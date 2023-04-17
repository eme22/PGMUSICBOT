package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

import org.springframework.stereotype.Component;

@Component
public class SendGlobalMessageCmd extends OwnerCommand {

    @Value("${config.aliases.sendom:}")
    String[] aliases = new String[0];

    Bot bot;

    public SendGlobalMessageCmd(Bot bot) {
        this.bot = bot;
        this.name = "sendom";
        this.help = "sends message from owner to the system channel of every server";
        this.arguments = "<message>";
        this.guildOnly = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "message", "Mensaje a enviar")
                .setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        String message = event.getOption("message", OptionMapping::getAsString);

        if (message == null) {
            event.reply(event.getClient().getError()+ " Mensaje Erroneo!!!").queue();
            return;
        }


        event.getJDA().getGuilds().forEach( guild -> {
            Server server = bot.getSettingsManager().getSettings(guild);
            guild.getTextChannelById(server.getTextChannelId()).sendMessage(message).queue();
        });

        event.reply("Mensaje Enviado!!!").queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        String message = event.getArgs();

        if (message == null) {
            event.replyError(" Mensaje Erroneo!!!");
            return;
        }

        event.getJDA().getGuilds().forEach( guild -> guild.getSystemChannel().sendMessage(message).queue());
    }
}
