package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class BotMessageCmd extends AdminCommand {

    @Value("${config.aliases.message:}")
    String[] aliases = new String[0];

    public BotMessageCmd(@Qualifier("adminCategory") Category category) {
        super(category);
        this.name = "message";
        this.help = "hace hablar al bot";
        this.arguments = "<mensaje>";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mensaje", "mensaje a decir").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String message = Objects.requireNonNull(event.getOption("mensaje")).getAsString();
        event.reply(event.getClient().getSuccess()+ " Mensaje Enviado").setEphemeral(true).queue();
        event.getChannel().sendMessage(message).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        String message = event.getArgs();
        if(message.isEmpty())
        {
            event.replyError(" Incluya un mensaje");
            return;
        }
        event.reply(message);
    }
}
