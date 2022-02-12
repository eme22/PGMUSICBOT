package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

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

            if (values < 0 || values > 100) {
                event.replyError("El valor tiene que ser entre 2 y  100!");
                return;
            }

            event.getMessage().delete();
            List<Message> messages = event.getChannel().getHistory().retrievePast(values).complete();
            event.getTextChannel().deleteMessages(messages).queue();
            event.getChannel().sendMessage("✅ " + values + "messages deleted!").queue(m ->
                    m.delete().queueAfter(5, TimeUnit.SECONDS));

        } catch (NumberFormatException ex) {
            event.replyError("Escribe un numero entre 2 y 100");
        }
    }
}
