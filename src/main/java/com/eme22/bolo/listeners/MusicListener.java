package com.eme22.bolo.listeners;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.audio.RequestMetadata;
import com.eme22.bolo.commands.music.LyricsCmd;
import com.eme22.bolo.entities.MusicPlayerEmoji;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.menu.Paginator;
import com.jagrosh.jlyrics.Lyrics;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.bolo.commands.music.QueueCmd.getQueueTitle;

@Component
public class MusicListener extends ListenerAdapter {

    private final Bot bot;
    private final Paginator.Builder builder;
    private int tempvolume = -1;

    @Value("${config.success}")
    private String successEmoji;
    @Value("${config.warning}")
    private String warningEmoji;

    @Autowired
    public MusicListener(Bot bot) {
        this.bot = bot;
        this.builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignore) {
                    }
                })
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);

    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {

        User user = event.getUser();
        Server settings = bot.getSettingsManager().getSettings(event.getGuild());

        if (user == null || user.isBot()) return;


        try {
            if (event.getMessageIdLong() == bot.getNowPlayingHandler().getLastNP(event.getGuild()).getValue()) {
                String reaction = event.getReaction().getEmoji().getAsReactionCode();

                MusicPlayerEmoji playerEmoji = MusicPlayerEmoji.isEmojiValid(reaction);

                if (playerEmoji != null){

                    VoiceChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel().asVoiceChannel();
                    if(current==null)
                        current = event.getGuild().getVoiceChannelById(settings.getVoiceChannelId());
                    if (OtherUtil.isUserInVoice(event.getGuild(), settings, event.getMember() )!= 1  ){
                        VoiceChannel finalCurrent = current;
                        event.getUser().openPrivateChannel().queue(success -> success.sendMessage("You must be listening in " + (finalCurrent == null ? "a voice channel" : finalCurrent.getAsMention()) + " to use that!").queue());
                        return;
                    }

                    switch (playerEmoji){
                        default: return;
                        case MUTE:
                            muteTogglePlayer(event, user); break;
                        case NEXT:
                            nextPlayer(event, user); break;
                        case PLAYORPAUSE:
                            playTogglePlayer(event, user); break;
                        case LYRICS:
                            showLyricsPlayer(event, user); break;
                        case QUEUE:
                            showQueueListPlayer(event, user); break;
                    }
                }

            }
        }

        catch (NullPointerException ignore){}


    }

    private void showLyricsPlayer(MessageReactionAddEvent event, User user) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        assert handler != null;
        String title = handler.getPlayer().getPlayingTrack().getInfo().title;
        if (title == null) {
            AudioHandler sendingHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (sendingHandler.isMusicPlaying(event.getJDA()))
                title = sendingHandler.getPlayer().getPlayingTrack().getInfo().title;
            else {
                event.getChannel().asTextChannel().sendMessage("There must be music playing to use that!").complete();
                return;
            }
        }

        event.getChannel().sendTyping().queue();

        Lyrics lyrics = OtherUtil.getLyrics(title);

        if(lyrics == null)
        {
            event.getChannel().asTextChannel().sendMessage("Lyrics for `" + title + "` could not be found!" + (title.isEmpty() ? " Try entering the song name manually (`lyrics [song name]`)" : "")).queue();
            return;
        }
        LyricsCmd.showLyrics(null, event.getGuild().getSelfMember().getColor(), event.getChannel().asTextChannel(), title, lyrics);
        try {
            event.getReaction().removeReaction(user).queue(s -> {}, t -> {});
        } catch (ErrorResponseException ignore) {}

    }

    private void showQueueListPlayer(@NotNull MessageReactionAddEvent event, User user) {
        int pagenum = 1;
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        assert handler != null;
        List<QueuedTrack> list = handler.getQueue().getList();
        if (list.isEmpty()) {
            MessageCreateData built = new MessageCreateBuilder()
                    .setContent(warningEmoji + " There is no music in the queue!")
                    .build();

            event.getChannel().asTextChannel().sendMessage(built).queue(m ->
                    m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        String[] songs = new String[list.size()];
        long total = 0;
        for (int i = 0; i < list.size(); i++) {
            total += list.get(i).getTrack().getDuration();
            songs[i] = list.get(i).toString();
        }
        Server settingsTEST = bot.getSettingsManager().getSettings(event.getGuild());
        long fintotal = total;
        builder.setText((i1, i2) -> {
                    assert settingsTEST != null;
                    return getQueueTitle(handler, successEmoji, songs.length, fintotal, settingsTEST.getRepeatMode());
                })
                .setItems(songs)
        ;
        builder.build().paginate(event.getChannel(), pagenum);
        try {
            event.getReaction().removeReaction(user).queue(s -> {}, t -> {});
        } catch (ErrorResponseException ignore) {}
    }

    private void playTogglePlayer(@NotNull MessageReactionAddEvent event, User user) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        handler.getPlayer().setPaused(!handler.getPlayer().isPaused());
        try {
            event.getReaction().removeReaction(user).queue(s -> {}, t -> {});
        } catch (ErrorResponseException ignore) {}
    }

    private void nextPlayer(@NotNull MessageReactionAddEvent event, User user) {
        AudioHandler handler = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler());
        assert handler != null;
        RequestMetadata rm = handler.getRequestMetadata();
        event.getChannel().asTextChannel().sendMessage(successEmoji + " Saltado: **" + handler.getPlayer().getPlayingTrack().getInfo().title
                + "** " + (rm.getOwner() == 0L ? "(autoplay)" : "(agregado por **" + event.getJDA().getUserById(rm.user.id).getAsMention() + "**) (saltado por **"+user.getAsMention()+"**)")).complete();
        handler.getPlayer().stopTrack();
        event.getReaction().removeReaction(user).queue(s -> {}, t -> {});
    }

    private void muteTogglePlayer(@NotNull MessageReactionAddEvent event, User user) {
        AudioPlayer player = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getPlayer();

        int volume = player.getVolume();

        if (volume == 0) {
            ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getPlayer().setVolume(tempvolume != 0 ? tempvolume : 50);
        } else {
            tempvolume = player.getVolume();
            ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getPlayer().setVolume(0);
        }

        try {
            event.getReaction().removeReaction(user).queue(s -> {}, t -> {});
        } catch (ErrorResponseException ignore) {}
    }

    private String formatTitle(String title) {
        Pattern pattern = Pattern.compile("((?:(?!(?:[\\)\\]\\/\\/])).)*\\s-\\s(?:(?!(?: [\\(\\[\\/\\\\])).)*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(title);
        boolean matchFound = matcher.find();
        if (matchFound) {
            return matcher.group(1);
        } else {
            System.out.println("Match not found");
            return title;
        }

    }
}
