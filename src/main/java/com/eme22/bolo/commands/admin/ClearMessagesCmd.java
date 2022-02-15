package com.eme22.bolo.commands.admin;

import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.eme22.bolo.Bot;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClearMessagesCmd extends AdminCommand {

    public ClearMessagesCmd(Bot bot)
    {
        this.name = "clear";
        this.help = "limpia los mensajes especificados";
        this.arguments = "<2 - 100>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            int values = Integer.parseInt(event.getArgs());

            if (values < 1 || values > 100) {
                event.replyError("El valor tiene que ser entre 1 y  100!");
                return;
            }

            event.getMessage().delete();
            List<Message> messages = event.getChannel().getHistory().retrievePast(values+1).complete();
            event.getTextChannel().deleteMessages(messages).queue();
            event.getChannel().sendMessage( event.getClient().getSuccess() +" " + values + " mensajes borrados!").queue(m ->
                    m.delete().queueAfter(5, TimeUnit.SECONDS));

        } catch (NumberFormatException ex) {
            event.replyError("Escribe un numero entre 1 y 100");
        }
    }
}
