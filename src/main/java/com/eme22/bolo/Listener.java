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
import com.eme22.bolo.commands.music.LyricsCmd;
import com.eme22.bolo.entities.RoleManager;
import com.eme22.bolo.settings.Settings;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.menu.Paginator;
import com.jagrosh.jlyrics.LyricsClient;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.bolo.commands.music.QueueCmd.getQueueTitle;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@SuppressWarnings("ConstantConditions")
public class Listener extends ListenerAdapter
{
    private final Bot bot;
    private final Paginator.Builder builder;

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
                catch(Exception ignored) {} // ignored
            }, 0, 24, TimeUnit.HOURS);
        }
    }

    @Override
    public void onGuildMessageEmbed(@NotNull GuildMessageEmbedEvent event) {
        ArrayList<TextChannel> bannedTextChannels = bot.getSettingsManager().getSettings(event.getGuild()).getOnlyImageChannels(event.getGuild());

        if (bannedTextChannels.contains(event.getChannel())) {
            List<MessageEmbed> message = event.getMessageEmbeds();

            AtomicBoolean deletable = new AtomicBoolean(true);


            message.forEach( messageEmbed -> {
                if (messageEmbed.getImage() != null || messageEmbed.getVideoInfo() != null)
                    deletable.set(false);
            });

            if (deletable.get())
                event.getChannel().deleteMessageById(event.getMessageId()).complete();
        }
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
    {
       ArrayList<TextChannel> bannedTextChannels = bot.getSettingsManager().getSettings(event.getGuild()).getOnlyImageChannels(event.getGuild());

        if (bannedTextChannels.contains(event.getChannel())) {
            Message message = event.getMessage();

            if(message.getContentRaw().contains("delimagechannel"))
                return;

            if (message.getContentRaw().contains("https://"))
                return;

            if (message.getAttachments().isEmpty())
                message.delete().complete();

        }
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) 
    {
        bot.getNowplayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
        bot.getSettingsManager().onGuildMessageDelete(event);
       // bot.getPollManager().onGuildMessageDelete(event);

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
    public void onShutdown(@NotNull ShutdownEvent event)
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
            sb.append("Seleccione el canal para ").append(title).append(": \n");
            for (int j = (i - 1) * 10; j < Math.min(i * 10, channels.size()); j++) {
                sb.append(OtherUtil.numtoString(j)).append(" ").append(channels.get(j).getName()).append("\n");
            }
            mb.setContent(sb.toString());
            pages.add(mb.build());
        }


        pages.forEach( page -> defaultChannel.sendMessage(page).queue(success -> {

            tempChannels.put(success.getId(), channel);
            for (int i = 0; i < getMessageItems(page); i++) {
                success.addReaction("U+003"+i+" U+FE0F U+20E3").queue();
            }
        }));

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

        bot.getSettingsManager().onGuildMessageReactionAdd(event, bot);

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
                    bot.getSettingsManager().getSettings(event.getGuild()).setTextChannelId(channelId.getIdLong());
                }
                if (mode == 1){
                    for (String key : getKeys(tempChannels, 1)) {

                        Message msgToDelete = event.getTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild()).setBienvenidasChannelId(channelId.getIdLong());
                }
                if (mode == 2){
                    for (String key : getKeys(tempChannels, 2)) {

                        Message msgToDelete = event.getTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild()).setDespedidasChannelId(channelId.getIdLong());
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
                    handler.getPlayer().setPaused(!handler.getPlayer().isPaused());
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
                            event.getTextChannel().sendMessage("Lyrics for `" + finalTitle + "` could not be found!" + (finalTitle.isEmpty() ? " Try entering the song name manually (`lyrics [song name]`)" : "")).queue();
                        else
                            LyricsCmd.showLyrics(null, event.getGuild().getSelfMember().getColor(), event.getTextChannel(),finalTitle, lyrics);
                    });
                    event.getReaction().removeReaction(event.getUser()).queue();
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
                                m.delete().queueAfter(5, TimeUnit.SECONDS));
                        return;
                    }
                    String[] songs = new String[list.size()];
                    long total = 0;
                    for(int i=0; i<list.size(); i++)
                    {
                        total += list.get(i).getTrack().getDuration();
                        songs[i] = list.get(i).toString();
                    }
                    Settings settingsTEST = bot.getSettingsManager().getSettings(event.getGuild());
                    long fintotal = total;
                    builder.setText((i1,i2) -> getQueueTitle(ah, bot.getConfig().getSuccess(), songs.length, fintotal, settingsTEST.getRepeatMode()))
                            .setItems(songs)
                    ;
                    builder.build().paginate(event.getChannel(), pagenum);
                    event.getReaction().removeReaction(event.getUser()).queue();
                }

            }
        }
        catch (NullPointerException ignore){}

        //System.out.println(event.getReactionEmote().getName());
        //System.out.println(event.getReactionEmote().getId());
        RoleManager manager = bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).getRoleManager(event.getMessageIdLong());

        if (manager != null) {
            String reaction = event.getReactionEmote().getAsReactionCode();
            //System.out.println("Emote for Search: "+event.getReactionEmote().getAsReactionCode());
            HashMap<String, String> datas = manager.getEmoji();
            datas.forEach((key, value) -> System.out.println(key + " " + value));

             if (datas.containsKey(event.getReactionEmote().getAsReactionCode())){
                 String roleT = datas.get(reaction);
                 //System.out.println("Role:"+ roleT);
                 List<Role> list = FinderUtil.findRoles(roleT, event.getGuild());
                 event.getGuild().addRoleToMember(event.getUserId(), list.get(0)).queue();
             }
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getUser().isBot())
            return;

        bot.getSettingsManager().onGuildMessageReactionRemove(event);

        RoleManager manager = bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).getRoleManager(event.getMessageIdLong());

        if (manager != null) {
            String reaction = event.getReactionEmote().getAsReactionCode();
            HashMap<String, String> datas = manager.getEmoji();

            if (datas.containsKey(reaction)){
                List<Role> list = FinderUtil.findRoles(datas.get(reaction), event.getGuild());
                event.getGuild().removeRoleFromMember(event.getUserId(), list.get(0)).complete();
            }
        }
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

        return message.getGuild().getTextChannelsByName(channam, true).get(0);
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
                    InputStream bienvenida = OtherUtil.getBackground(bot.getSettingsManager().getSettings(guild), true);
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

                    OtherUtil.createImage( "BIENVENIDO", member.getName(), member.getId(), bienvenida, userImage);
                    if (!converted.exists()){
                        Logger log = LoggerFactory.getLogger("MusicBot");
                        log.error("Image not created");
                    }

                    String message = OtherUtil.getMessage(bot, guild, true );
                    message = message.replaceAll("@username", member.getAsMention()).replaceAll("@servername", guild.getName());

                    //builder.setThumbnail("attachment://bienvenida.png");
                    bienvenidas.sendMessage( message ).addFile(converted).complete();
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
                    InputStream despedida = OtherUtil.getBackground(bot.getSettingsManager().getSettings(guild), false);

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

                    String message = OtherUtil.getMessage(bot, guild, false );
                    message = message.replaceAll("@username", member.getAsMention()).replaceAll("@servername", guild.getName());
                    //builder.setThumbnail("attachment://bienvenida.png");
                    despedidas.sendMessage(message).addFile(converted).complete();
                    converted.delete();

                }
            }
            catch(Exception exception) {
                Logger log = LoggerFactory.getLogger("MusicBot");
                log.error("Error: "+ exception.getMessage(), exception);
            }
    }

}
