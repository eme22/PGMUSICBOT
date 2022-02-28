package com.eme22.bolo.poll;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.Poll;
import com.eme22.bolo.utils.OtherUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PollManager {

    private Map<Long,Map<Long, Poll>> polls;
    private final Bot bot;

    public PollManager(Bot bot) {
        this.bot = bot;
        try {
            Reader reader = Files.newBufferedReader(OtherUtil.getPath("serverpolls.json"));
            Type type = new TypeToken<Map<Long,Map<Long, Poll>>>(){}.getType();
            Map<Long,Map<Long, Poll>> pollsTemp = new Gson().fromJson(reader, type);
            if (pollsTemp == null){
                this.polls = new HashMap<>();
                throw new IOException();
            }
            else
                this.polls = pollsTemp;

            //pollsTemp.forEach( (aLong, pollMap) -> pollMap.forEach( (mssgid, pollMap2) -> pollMap.put(mssgid, new Poll(pollMap2.getQuestion(), pollMap2.getAnswers()))));
            reader.close();

        } catch( IOException e) {
            LoggerFactory.getLogger("Polls").warn("Failed to load server polls (this is normal if no polls have been set yet): "+e);
        }
    }

    public Map<Long, Poll> getPollForGuild(Guild guild){
        return polls.get(guild.getIdLong());
    }

    public Map<Long, Map<Long, Poll>> getPolls() {
        return polls;
    }

    public void addPollForGuild(Guild guild, Long messageId, Poll poll){

        Map<Long, Poll> polls = this.polls.computeIfAbsent( guild.getIdLong(), guildId -> new HashMap<>());
        polls.put(messageId, poll);
        writePolls();
    }

    public void onGuildMessageReactionAdd(@NotNull MessageReactionAddEvent event) {

        if (polls.containsKey(event.getGuild().getIdLong())){
            Map<Long, Poll> pollMap = getPollForGuild(event.getGuild());
            if (pollMap.containsKey(event.getMessageIdLong())){
                String emoji = event.getReaction().getReactionEmote().getEmoji();
                if (isCorrectEmoji(emoji)){
                    if (pollMap.get(event.getMessageIdLong()).hasUserParticipated(event.getUserIdLong())){
                        event.getUser().openPrivateChannel().queue( success -> success.sendMessage(bot.getConfig().getError()+" Solo puedes votar una vez").queue(m ->
                                m.delete().queueAfter(30, TimeUnit.SECONDS)));
                        event.getReaction().removeReaction(event.getUser()).queue();
                    }
                    else
                        updatePoll(OtherUtil.EmojiToNumber(emoji), event, true);
                }
            }
        }

    }

    public void onGuildMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {

        if (this.polls.containsKey(event.getGuild().getIdLong())){
            Map<Long, Poll> pollMap = getPollForGuild(event.getGuild());
            if (pollMap.containsKey(event.getMessageIdLong())){
                String emoji = event.getReaction().getReactionEmote().getEmoji();
                int num = OtherUtil.EmojiToNumber(emoji);
                long userid =  event.getUserIdLong();
                if (isCorrectEmoji(emoji) && pollMap.get(event.getMessageIdLong()).isUserParticipant(num, userid)){
                    updatePoll(num, event, false);
                }
            }
        }

    }

    private boolean isCorrectEmoji(String emoji) {
        return OtherUtil.EmojiToNumber(emoji) != -1;
    }

    private void updatePoll(int num, @NotNull GenericMessageReactionEvent event, boolean add){
        Long messageId = event.getMessageIdLong();
        Map<Long, Poll> pollMap = this.polls.get(event.getGuild().getIdLong());
        Poll poll = pollMap.get(messageId);
        if (add)
            poll.addVoteToAnswer(num, event.getUserIdLong());
        else
            poll.removeVoteFromAnswer(num, event.getUserIdLong());

        event.getTextChannel().
                editMessageEmbedsById(
                event.getMessageId(),
                new EmbedBuilder()
                        .setDescription(OtherUtil.makePollString(poll))
                        .build()
                ).queue();

        writePolls();
    }

    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        if (this.polls.containsKey(event.getGuild().getIdLong())){
            getPollForGuild(event.getGuild()).remove(event.getMessageIdLong());
            writePolls();
        }
    }

    public void writePolls()
    {
        try {
            FileWriter writer = new FileWriter(OtherUtil.getPath("serverpolls.json").toFile());
            new GsonBuilder().setPrettyPrinting().create().toJson(this.polls, writer);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            LoggerFactory.getLogger("Settings").warn("Failed to write to file: "+ex);
        }
    }
}
