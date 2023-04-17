package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
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
public class SetGoodByeMessageCmd extends AdminCommand {

    @Value("${config.aliases.setgoodbyemessage:}")
    String[] aliases = new String[0];

    public SetGoodByeMessageCmd(@Qualifier("adminCategory") Category category)
    {
        super(category);
        this.name = "setgoodbyemsg";
        this.help = "cambia el mensaje de despedida";
        this.arguments = "<message>";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mensaje", "mensaje a decir cuando un usuario abandona el servidor.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String message = Objects.requireNonNull(event.getOption("mensaje")).getAsString();
        Server s = event.getClient().getSettingsFor(event.getGuild());
        s.setDespedidasChannelMessage(message);
        event.reply(event.getClient().getSuccess() + "El mensaje de despedida es ahora: \n" + "\"" + message + "\"").queue();
        s.save();
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String image = event.getArgs();
        if(image.isEmpty()) {
            event.replyError(" Incluya un texto");
            return;
        }
        Server s = event.getClient().getSettingsFor(event.getGuild());
        s.setDespedidasChannelMessage(image);
        event.replySuccess(" El mensaje de despedida es ahora: \n"+"\""+image+"\"");
        s.save();
    }
}
