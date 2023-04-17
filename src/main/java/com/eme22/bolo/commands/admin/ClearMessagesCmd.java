package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class ClearMessagesCmd extends AdminCommand {

    @Value("${config.aliases.clear:}")
    String[] aliases = new String[0];

    public ClearMessagesCmd(@Qualifier("adminCategory") Category category)
    {
        super(category);
        this.name = "clear";
        this.help = "limpia los mensajes especificados";
        this.arguments = "<2 - 100>";
        this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "mensajes", "numero entre 2 al 100")
                        .setMinValue(2)
                        .setMaxValue(100)
                        .setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Integer values = event.getOption("mensajes", OptionMapping::getAsInt);
        List<Message> messages = event.getChannel().getHistory().retrievePast(values).complete();
        event.getTextChannel().deleteMessages(messages).queue();
        event.reply(event.getClient().getSuccess() +" " + values + " mensajes borrados!").setEphemeral(true).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            int values = Integer.parseInt(event.getArgs());

            if (values < 1 || values > 100) {
                event.replyError("El valor tiene que ser entre 1 y  100!");
                return;
            }

            //event.getMessage().delete();
            List<Message> messages = event.getChannel().getHistory().retrievePast(values+1).complete();
            event.getTextChannel().deleteMessages(messages).queue();
            event.getChannel().sendMessage( event.getClient().getSuccess() +" " + values + " mensajes borrados!").queue(m ->
                    m.delete().queueAfter(5, TimeUnit.SECONDS));

        } catch (NumberFormatException ex) {
            event.replyError("Escribe un numero entre 1 y 100");
        }
    }
}
