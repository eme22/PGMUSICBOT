package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PollCmd extends AdminCommand {

    public PollCmd(Bot bot) {

        this.name = "cleardata";
        this.help = "limpia todos los datos del servidor";
        this.arguments = "[Question] [Answer 1] [Answer 2]...[Answer 9]";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" Por favor incluya al menos una Pregunta y 2 respuestas en la pregunta");
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile("\\[(.*?)\\]");
        Matcher m = p.matcher(event.getArgs());

        final int[] i = {1};
        while(m.find()) {
            if (i[0] >= 10)
                break;

            if (i[0] == 1){
                sb.append("\uD83D\uDCCA").append(" ").append(m.group(i[0])).append("\n");
            }
            else {
                sb.append(OtherUtil.numtoString(i[0])).append(" ").append(m.group(i[0])).append("\n");
            }

            i[0]++;
        }

        eb.setDescription(sb.toString());
        event.getTextChannel().sendMessageEmbeds(eb.build()).queue(success -> {
            if (i[0] == 10) i[0] = 9;

            for (int j = 0; j < i[0]; j++) {
                success.addReaction("U+003"+j+" U+FE0F U+20E3").queue();
            }

            success.addReaction("\uD83D\uDCDD").queue();

        });

    }
}
