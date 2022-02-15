/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eme22.bolo;

import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.audio.RequestMetadata;
import com.eme22.bolo.commands.music.QueueCmd;
import com.eme22.bolo.settings.Settings;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import com.jagrosh.jlyrics.LyricsClient;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.bolo.commands.music.QueueCmd.getQueueTitle;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Listener extends ListenerAdapter
{
    private final Bot bot;
    private Paginator.Builder builder;

    public Listener(Bot bot)
    {
        this.bot = bot;
        this.builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {try{m.clearReactions().queue();}catch(PermissionException ignore){}})
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);
    }

    private final HashMap<String, Integer> tempChannels = new HashMap<>();

    private int tempvolume = -1;
    
    @Override
    public void onReady(ReadyEvent event) 
    {
        if(event.getJDA().getGuildCache().isEmpty())
        {
            Logger log = LoggerFactory.getLogger("MusicBot");
            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().getInviteUrl(Bolo.RECOMMENDED_PERMS));
        }
        //credit(event.getJDA());
        event.getJDA().getGuilds().forEach((guild) -> 
        {
            try
            {
                String defpl = bot.getSettingsManager().getSettings(guild).getDefaultPlaylist();
                VoiceChannel vc = bot.getSettingsManager().getSettings(guild).getVoiceChannel(guild);
                if(defpl!=null && vc!=null && bot.getPlayerManager().setUpHandler(guild).playFromDefault())
                {
                    guild.getAudioManager().openAudioConnection(vc);
                }
            }
            catch(Exception ignore) {}
        });
        if(bot.getConfig().useUpdateAlerts())
        {
            bot.getThreadpool().scheduleWithFixedDelay(() -> 
            {
                try
                {
                    User owner = bot.getJDA().retrieveUserById(bot.getConfig().getOwnerId()).complete();
                    String currentVersion = OtherUtil.getCurrentVersion();
                    String latestVersion = OtherUtil.getLatestVersion();
                    if(latestVersion!=null && !currentVersion.equalsIgnoreCase(latestVersion))
                    {
                        String msg = String.format(OtherUtil.NEW_VERSION_AVAILABLE, currentVersion, latestVersion);
                        owner.openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
                    }
                }
                catch(Exception ex) {} // ignored
            }, 0, 24, TimeUnit.HOURS);
        }
    }
    
    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) 
    {
        bot.getNowplayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event)
    {
        bot.getAloneInVoiceHandler().onVoiceUpdate(event);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event)
    {
        if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong())
            return;

        Guild guild = event.getGuild();
        ((AudioHandler) guild.getAudioManager().getSendingHandler()).stopAndClear();
        guild.getAudioManager().closeAudioConnection();
    }

    @Override
    public void onShutdown(ShutdownEvent event) 
    {
        bot.shutdown();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) 
    {
        Guild guild = event.getGuild();
        try
            {
                TextChannel commandsChannel = bot.getSettingsManager().getSettings(guild).getTextChannel(guild);
                TextChannel bienvenidasChannel = bot.getSettingsManager().getSettings(guild).getHelloChannel(guild);
                TextChannel despedidasChannel = bot.getSettingsManager().getSettings(guild).getGoodbyeChannel(guild);
                TextChannel defaultChannel = guild.getDefaultChannel();
                List<TextChannel> channels = guild.getTextChannels();

                if (commandsChannel == null){
                    setupChannel("Comandos de Musica",defaultChannel, channels, 0);
                }
                if (bienvenidasChannel == null){
                    setupChannel("Bienvenidas", defaultChannel, channels, 1);
                }
                if (despedidasChannel == null){
                    setupChannel("Despedidas", defaultChannel, channels, 2);
                }
            }
        catch(Exception exception) {
            Logger log = LoggerFactory.getLogger("MusicBot");
            log.error("Error: "+ exception.getMessage(), exception);
        }

    }

    private void setupChannel(String title, TextChannel defaultChannel, List<TextChannel> channels, int channel) {
        ArrayList<Message> pages = new ArrayList<>();
        int calculatedPages = (int) Math.ceil( (double) channels.size() / 10);
        MessageBuilder mb = new MessageBuilder();
        for (int i = 1; i <= calculatedPages; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("Seleccione el canal para "+title+": \n");
            for (int j = (i - 1) * 10; j < Math.min(i * 10, channels.size()); j++) {
                sb.append(OtherUtil.numtoString(j)).append(" ").append(channels.get(j).getName()).append("\n");
            }
            mb.setContent(sb.toString());
            pages.add(mb.build());
        }


        pages.forEach( page -> {

            defaultChannel.sendMessage(page).queue(success -> {

                tempChannels.put(success.getId(), channel);
                for (int i = 0; i < getMessageItems(page); i++) {
                    success.addReaction("U+003"+i+" U+FE0F U+20E3").queue();
                }
            });

        });

    }

    private int getMessageItems(Message message) {
        String[] chans = message.getContentRaw().split("\n");
        chans = Arrays.copyOfRange(chans, 1, chans.length);
        return chans.length;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        if (tempChannels.containsKey(event.getMessageId())){

            String reaction = event.getReactionEmote().getName();
            int channel = Integer.parseInt(reaction.replaceAll("[^\\d.]", ""));
            TextChannel channelId = getChannelFromMessage(channel, event.retrieveMessage().complete());
            if (channelId != null) {

                int mode = tempChannels.get(event.getMessageId());
                if (mode == 0){
                    for (String key : getKeys(tempChannels, 0)) {

                        Message msgToDelete = event.getTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild()).setTextChannel(channelId);
                }
                if (mode == 1){
                    for (String key : getKeys(tempChannels, 1)) {

                        Message msgToDelete = event.getTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild()).setHelloID(channelId);
                }
                if (mode == 2){
                    for (String key : getKeys(tempChannels, 2)) {

                        Message msgToDelete = event.getTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild()).setGoodByeID(channelId);
                }
            }


        }

        try {

            if(event.getMessageIdLong() == bot.getNowplayingHandler().getLastNP(event.getGuild()).getValue()) {
                String reaction = event.getReactionEmote().getAsReactionCode();

                if (reaction.equals("\uD83D\uDD07")) {
                    AudioPlayer player = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getPlayer();

                    int volume = player.getVolume();

                    if (volume == 0) {
                        ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getPlayer().setVolume(tempvolume != 0 ? tempvolume : 50);
                    } else {
                        tempvolume = player.getVolume();
                        ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getPlayer().setVolume(0);
                    }

                    event.getReaction().removeReaction(event.getUser()).complete();

                }
                if (reaction.equals("⏭")) {
                    AudioHandler handler = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler());
                    RequestMetadata rm = handler.getRequestMetadata();
                    event.getTextChannel().sendMessage(bot.getConfig().getSuccess()+" Skipped **"+handler.getPlayer().getPlayingTrack().getInfo().title
                            +"** "+(rm.getOwner() == 0L ? "(autoplay)" : "(requested by **" + rm.user.username + "**)")).complete();
                    handler.getPlayer().stopTrack();
                    event.getReaction().removeReaction(event.getUser()).complete();
                }

                if (reaction.equals("⏯")) {

                    AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
                    if(handler.getPlayer().isPaused())
                    {
                        handler.getPlayer().setPaused(false);
                        event.getTextChannel().sendMessage("Resumido **"+handler.getPlayer().getPlayingTrack().getInfo().title+"**.");
                    }
                    else
                    {
                        handler.getPlayer().setPaused(true);
                        event.getTextChannel().sendMessage("Paused **"+handler.getPlayer().getPlayingTrack().getInfo().title+"**. Type `"+bot.getConfig().getPrefix()+"play` to unpause!");

                    }
                    event.getReaction().removeReaction(event.getUser()).complete();
                }

                if (reaction.equals("\uD83C\uDFB5")){
                    AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
                    String title = handler.getPlayer().getPlayingTrack().getInfo().title;
                    if(title == null)
                    {
                        AudioHandler sendingHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                        if (sendingHandler.isMusicPlaying(event.getJDA()))
                            title = sendingHandler.getPlayer().getPlayingTrack().getInfo().title;
                        else
                        {
                            event.getTextChannel().sendMessage ("There must be music playing to use that!").complete();
                            return;
                        }
                    }

                    event.getChannel().sendTyping().queue();
                    String finalTitle = formatTitle(title);
                    new LyricsClient().getLyrics(title).thenAccept(lyrics ->
                    {
                        if(lyrics == null)
                        {
                            event.getTextChannel().sendMessage("Lyrics for `" + finalTitle + "` could not be found!" + (finalTitle.isEmpty() ? " Try entering the song name manually (`lyrics [song name]`)" : "")).complete();
                            return;
                        }

                        EmbedBuilder eb = new EmbedBuilder()
                                .setAuthor(lyrics.getAuthor())
                                .setColor(event.getGuild().getSelfMember().getColor())
                                .setTitle(lyrics.getTitle(), lyrics.getURL());
                        if(lyrics.getContent().length()>15000)
                        {
                            event.getTextChannel().sendMessage("Lyrics for `" + finalTitle + "` found but likely not correct: " + lyrics.getURL()).complete();
                        }
                        else if(lyrics.getContent().length()>2000)
                        {
                            String content = lyrics.getContent().trim();
                            while(content.length() > 2000)
                            {
                                int index = content.lastIndexOf("\n\n", 2000);
                                if(index == -1)
                                    index = content.lastIndexOf("\n", 2000);
                                if(index == -1)
                                    index = content.lastIndexOf(" ", 2000);
                                if(index == -1)
                                    index = 2000;
                                event.getTextChannel().sendMessage(eb.setDescription(content.substring(0, index).trim()).build()).complete();
                                content = content.substring(index).trim();
                                eb.setAuthor(null).setTitle(null, null);
                            }
                            event.getTextChannel().sendMessage(eb.setDescription(content).build()).complete();
                        }
                        else
                            event.getTextChannel().sendMessage(eb.setDescription(lyrics.getContent()).build()).complete();
                    });
                    event.getReaction().removeReaction(event.getUser()).complete();
                }

                if (reaction.equals("\uD83D\uDCC3"))
                {
                    int pagenum = 1;
                    AudioHandler ah = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
                    List<QueuedTrack> list = ah.getQueue().getList();
                    if(list.isEmpty())
                    {
                        Message built = new MessageBuilder()
                                .setContent(bot.getConfig().getWarning() + " There is no music in the queue!")
                                .build();

                        event.getTextChannel().sendMessage(built).queue(m ->
                        {
                            m.delete().queueAfter(5, TimeUnit.SECONDS);
                        });
                        return;
                    }
                    String[] songs = new String[list.size()];
                    long total = 0;
                    for(int i=0; i<list.size(); i++)
                    {
                        total += list.get(i).getTrack().getDuration();
                        songs[i] = list.get(i).toString();
                    }
                    Settings settings = bot.getSettingsManager().getSettings(event.getGuild());
                    long fintotal = total;
                    builder.setText((i1,i2) -> getQueueTitle(ah, bot.getConfig().getSuccess(), songs.length, fintotal, settings.getRepeatMode()))
                            .setItems(songs)
                    ;
                    builder.build().paginate(event.getChannel(), pagenum);
                }
            }
        }
        catch (NullPointerException ignore){}
    }

    private String formatTitle(String title) {
        Pattern pattern = Pattern.compile("((?:(?!(?:[\\)\\]\\/\\/])).)*\\s-\\s(?:(?!(?: [\\(\\[\\/\\\\])).)*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(title);
        boolean matchFound = matcher.find();
        if(matchFound) {
            return matcher.group(1);
        } else {
            System.out.println("Match not found");
            return title;
        }

    }

    private TextChannel getChannelFromMessage(int channel, Message message) {
        String[] chans = message.getContentRaw().split("\n");
        chans = Arrays.copyOfRange(chans, 1, chans.length);

        if (channel > chans.length)
            return null;

        String channam = chans[channel].split(":")[2].substring(1);

        TextChannel chan = message.getGuild().getTextChannelsByName(channam, true).get(0);
        if (chan == null)
            return null;
        else return chan;
    }

    private static Set<String> getKeys(Map<String, Integer> map, Integer value) {

        Set<String> result = new HashSet<>();
        if (map.containsValue(value)) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (Objects.equals(entry.getValue(), value)) {
                    result.add(entry.getKey());
                }
            }
        }
        return result;

    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        User member = event.getMember().getUser();
            try
            {
                TextChannel bienvenidas = bot.getSettingsManager().getSettings(guild).getHelloChannel(guild);

                if(bienvenidas != null)
                {
                    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                    InputStream bienvenida = classloader.getResourceAsStream("images/bienvenida.png");
                    String userImage = member.getAvatarUrl();
                    if (userImage == null)
                        userImage = member.getDefaultAvatarUrl();
                    else
                        userImage = member.getAvatarUrl();

                    File parent = new File("temp");
                    if(!parent.exists()) {
                        parent.mkdirs();
                    }

                    File converted = new File(parent, member.getId()+".png");
                    if (converted.exists())
                        converted.delete();

                    OtherUtil.createImage( "BIENVENIDO", member.getName(), String.valueOf(member.getId()), bienvenida, userImage);
                    if (!converted.exists()){
                        Logger log = LoggerFactory.getLogger("MusicBot");
                        log.error("Image not created");
                    }


                    StringBuilder builder = new StringBuilder();
                    //builder.setThumbnail("attachment://bienvenida.png");
                    builder.append("En que andas " + member.getAsMention() + "te damos la bienvenida a " + guild.getName() + " , Discord creado por el pueblo y para el pueblo.\nPARA INGRESAR A LOS OTROS CANALES, POR FAVOR LEE LAS "+ guild.getTextChannelById("869307625169367130").getAsMention() + " Y VERIFÍCATE.");
                    bienvenidas.sendMessage(builder.toString()).addFile(converted).complete();
                    converted.delete();
                }
            }
            catch(Exception exception) {
                Logger log = LoggerFactory.getLogger("MusicBot");
                log.error("Error: "+ exception.getMessage(), exception);
            }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        User member = event.getMember().getUser();

            try
            {
                TextChannel despedidas = bot.getSettingsManager().getSettings(guild).getGoodbyeChannel(guild);
                if(despedidas!=null)
                {
                    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                    InputStream despedida = classloader.getResourceAsStream("images/despedida.png");

                    String userImage = member.getAvatarUrl();
                    if (userImage == null)
                        userImage = member.getDefaultAvatarUrl();
                    else
                        userImage = member.getAvatarUrl();

                    File parent = new File("temp");
                    if(!parent.exists()) {
                        parent.mkdirs();
                    }

                    File converted = new File(parent, member.getId()+".png");
                    if (converted.exists())
                        converted.delete();

                    OtherUtil.createImage( "SE VA", member.getName(), String.valueOf(member.getId()), despedida, userImage);
                    if (!converted.exists()){
                        Logger log = LoggerFactory.getLogger("MusicBot");
                        log.error("Image not created");
                    }


                    StringBuilder builder = new StringBuilder();
                    //builder.setThumbnail("attachment://bienvenida.png");
                    builder.append("Se ha escapado de nuestras manos");
                    despedidas.sendMessage(builder.toString()).addFile(converted).complete();
                    converted.delete();

                }
            }
            catch(Exception exception) {
                Logger log = LoggerFactory.getLogger("MusicBot");
                log.error("Error: "+ exception.getMessage(), exception);
            }
    }
}
