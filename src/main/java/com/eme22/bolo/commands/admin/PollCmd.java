package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.entities.Answer;
import com.eme22.bolo.entities.Poll;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PollCmd extends AdminCommand {

    protected final Bot bot;

    public PollCmd(Bot bot) {

        this.bot = bot;
        this.name = "poll";
        this.help = "crea una votacion con los datos enviados";
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

        Poll poll = new Poll();

        Pattern p = Pattern.compile("\\[(.*?)\\]");
        Matcher m = p.matcher(event.getArgs());

        final int[] i = {1};
        while(m.find()) {
            if (i[0] >= 10)
                break;
            if (i[0] == 1) {
                    poll.setQuestion(m.group(1));
            }
            else {
                Answer answers = new Answer(m.group(1), new HashSet<>());
                poll.addAnswer(answers);
            }

            i[0]++;
        }

        if (i[0] == 10)
            i[0] = 9;

        EmbedBuilder eb = new EmbedBuilder();

        int answers = poll.getAnswers().size();
        eb.setDescription(OtherUtil.makePollString(poll));
        event.getTextChannel().sendMessageEmbeds(eb.build()).queue(success -> {

           for (int j = 0; j < answers; j++) {
                success.addReaction("U+003"+j+" U+FE0F U+20E3").queue();
            }

            //success.addReaction("\uD83D\uDCDD").queue();
            bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).addPollForGuild(success.getIdLong(), poll);
            //pollManager.addPollForGuild(event.getGuild(), success.getIdLong(), poll);
        });

    }
}
