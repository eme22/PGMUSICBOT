package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotMessageCmd extends AdminCommand {

    public BotMessageCmd(Bot bot) {
        this.name = "message";
        this.help = "hace hablar al bot";
        this.arguments = "<mensaje>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        String message = event.getArgs();
        if(message.isEmpty())
        {
            event.replyError(" Incluya un mensaje");
            return;
        }

        Pattern regex = Pattern.compile("(.+?(?=]).|.*)", Pattern.DOTALL);
        Matcher matcher = regex.matcher(message);
        while (matcher.find()){
            String nextmessage = matcher.group(1);
            if (nextmessage == null || nextmessage.isBlank())
                continue;
            Pattern regex1 = Pattern.compile("\\[([^\\[]*)\\]");
            Matcher matcher1 = regex1.matcher(nextmessage);
            if (matcher1.find()){
                nextmessage = nextmessage.replace(matcher1.group(), "");
                event.getTextChannel().sendMessage(nextmessage + " " + matcher1.group(1) ).complete();
            } else {
                event.getTextChannel().sendMessage(nextmessage).complete();
            }
        }
    }
}
