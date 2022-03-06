package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotEmbbedMessageCmd extends AdminCommand {

    public BotEmbbedMessageCmd(Bot bot) {
        this.name = "message2";
        this.help = "hace hablar al bot con mensajes embedidos";
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

        EmbedBuilder builder;

        Pattern regex = Pattern.compile("]?([^]]*)");
        Matcher matcher = regex.matcher(message);
        while (matcher.find()){
            String nextmessage = matcher.group(1);
            if (nextmessage.isBlank())
                continue;
            Pattern regex1 = Pattern.compile("\\[([^\\[]*)\\]");
            Matcher matcher1 = regex1.matcher(nextmessage);
            builder = new EmbedBuilder();
            if (matcher1.find()){
                nextmessage = nextmessage.replace(matcher1.group(), "");
                builder.setDescription(nextmessage).setImage(matcher1.group(1));
            } else {
                builder.setDescription(nextmessage);
            }
            event.getTextChannel().sendMessageEmbeds(builder.build()).complete();
        }
    }
}
