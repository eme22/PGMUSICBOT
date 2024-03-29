package com.eme22.bolo.listeners;

import com.eme22.bolo.Bot;
import com.eme22.bolo.model.Poll;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.OtherUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class PollListener extends ListenerAdapter {

    private final Bot bot;

    @Value("${config.error}")
    private String errorEmoji;

    @Autowired
    public PollListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        if (!event.isFromGuild())
            return;

        Server settings = bot.getSettingsManager().getSettings(event.getGuild());
        if (settings != null && settings.getPolls().stream().anyMatch(poll -> poll.getId() == event.getMessageIdLong())) {

            Poll polls = settings.getPolls().stream().filter(poll -> poll.getId() == event.getMessageIdLong()).findFirst().orElse(null);

            int num = OtherUtil.EmojiToNumber(event.getReaction().getEmoji().asUnicode().getFormatted());

            if (polls != null && num != -1 && polls.isUserParticipating(event.getUserIdLong())) {
                event.getUser().openPrivateChannel().queue(success -> success.sendMessage(errorEmoji + " Solo puedes votar una vez").queue(m ->
                        m.delete().queueAfter(30, TimeUnit.SECONDS)));
                event.getReaction().removeReaction(event.getUser()).queue();
            } else {
                if (polls != null) {

                    polls.addVoteToAnswer(num, event.getUserIdLong());
                    settings.save();

                    event.getChannel().
                            editMessageEmbedsById(
                                    event.getMessageId(),
                                    new EmbedBuilder()
                                            .setDescription(OtherUtil.makePollString(polls))
                                            .build()
                            ).queue();
                }

            }
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getUser().isBot())
            return;

        if (!event.isFromGuild())
            return;

        Server settings = bot.getSettingsManager().getSettings(event.getGuild());
        if (settings != null && settings.getPolls().stream().anyMatch(poll -> poll.getId() == event.getMessageIdLong())) {
            Poll polls = settings.getPolls().stream().filter(poll -> poll.getId() == event.getMessageIdLong()).findFirst().orElse(null);

            int num = OtherUtil.EmojiToNumber(event.getReaction().getEmoji().asUnicode().getFormatted());

            if (polls != null && num != -1 && polls.isUserParticipating(event.getUserIdLong())) {

                if (polls.isUserParticipatingInAnswer(num, event.getUserIdLong())) {


                    polls.removeVoteFromAnswer(num, event.getUserIdLong());
                    settings.save();

                    event.getChannel().
                            editMessageEmbedsById(
                                    event.getMessageId(),
                                    new EmbedBuilder()
                                            .setDescription(OtherUtil.makePollString(polls))
                                            .build()
                            ).queue();

                }
            }
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {

        if (!event.isFromGuild())
            return;

        Server settings = bot.getSettingsManager().getSettings(event.getGuild());
        if (settings != null) {
            settings.removePollFromGuild(event.getMessageIdLong());
            settings.deleteRoleManagers(event.getMessageIdLong());
            settings.save();
        }
    }
}
