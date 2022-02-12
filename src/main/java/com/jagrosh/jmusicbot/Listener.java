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
package com.jagrosh.jmusicbot;

import com.jagrosh.jlyrics.LyricsClient;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
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
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.jagrosh.jmusicbot.utils.OtherUtil.numtoString;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Listener extends ListenerAdapter
{
    private final Bot bot;
    
    public Listener(Bot bot)
    {
        this.bot = bot;
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
            log.warn(event.getJDA().getInviteUrl(JMusicBot.RECOMMENDED_PERMS));
        }
        credit(event.getJDA());
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
                sb.append(numtoString(j)).append(" ").append(channels.get(j).getName()).append("\n");
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
                Logger log = LoggerFactory.getLogger("MusicBot");
                log.error("Emoji: " + reaction);

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
                    String finalTitle = title;
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

                }
            }
        }
        catch (NullPointerException ignore){}
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
                    String bienvenida = classloader.getResource("images/bienvenida.png").getFile();
                    String userimage = member.getAvatarUrl();
                    if (userimage == null)
                        userimage = member.getDefaultAvatarUrl();
                    File converted = new File("temp",member.getId()+".png");
                    if (converted.exists())
                        converted.delete();
                    bienvenida(member.getId(), bienvenida, member.getName(),  userimage);
                    if (!converted.exists()){
                        Logger log = LoggerFactory.getLogger("MusicBot");
                        log.error("Image not created");
                    }


                    StringBuilder builder = new StringBuilder();
                    //builder.setThumbnail("attachment://bienvenida.png");
                    builder.append("En que andas " + member.getAsMention() + "te damos la bienvenida a " + guild.getName() + " , Discord creado por el pueblo y para el pueblo.\nPARA INGRESAR A LOS OTROS CANALES, POR FAVOR LEE LAS"+ guild.getTextChannelsByName("\uD835\uDE4D\uD835\uDE40\uD835\uDE42\uD835\uDE47\uD835\uDE3C\uD835\uDE4E", true).get(0).getAsMention() + " Y VERIFÍCATE.");
                    bienvenidas.sendMessage(builder.toString()).queue();
                    bienvenidas.sendFile(converted).queue();
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
                    String despedida = classloader.getResource("images/despedida.png").getFile();
                    String userimage = member.getAvatarUrl();
                    if (userimage == null)
                        userimage = member.getDefaultAvatarUrl();
                    File converted = new File("temp",member.getId()+".png");
                    if (converted.exists())
                        converted.delete();
                    despedida(member.getId(), despedida, member.getName(),  userimage);
                    if (!converted.exists()){
                        Logger log = LoggerFactory.getLogger("MusicBot");
                        log.error("Image not created");
                    }

                    StringBuilder builder = new StringBuilder();
                    //builder.setThumbnail("attachment://bienvenida.png");
                    builder.append( member.getAsMention() + "Se ha escapado de nuestras manos!!!");
                    despedidas.sendMessage(builder.toString()).queue();
                    despedidas.sendFile(converted).queue();
                }
            }
            catch(Exception exception) {
                Logger log = LoggerFactory.getLogger("MusicBot");
                log.error("Error: "+ exception.getMessage(), exception);
            }
    }


    private static void bienvenida(String userid, String path, String name, String userimage) {

        String html = "<body>\n" +
                "\t<div style=\"background-image:url('file:"+path+"'); justify-content: flex-start; background-position: 0% 0%; background-size: 100px 100px; background-repeat: no-repeat; width: 63em;\n" +
                "  height: 31em;\">\n" +
                "\t\t<div style=\"justify-content: left; text-align:left; margin-left: 420px; margin-top: 20px;\">\n" +
                "\t\t\t<img style=\"vertical-align: middle; width: 200px; height: 200px; border-radius: 50%;\" src='"+ userimage +"'>\n" +
                "\t\t</div>\n" +
                "\t\n" +
                "\t\t<h1 style=\"text-align:middle; font-size: 60px; font-family: Arial, Helvetica, sans-serif; margin-left: 350px;\">\n" +
                "\t\t\tBienvenido!!!\n" +
                "\t\t</h1>\n" +
                "\t\t<h1 style=\"text-align:middle;  font-size: 60px;font-family: Arial, Helvetica, sans-serif; margin-left: 350px;\">\n" +
                "\t\t\t"+name+"\n" +
                "\t\t</h1>\n" +
                "\t\t<h2 style=\"text-align:middle; font-size: 30px; font-family: Arial, Helvetica, sans-serif; margin-left: 240px;\">\n" +
                "\t\t\tUn nuevo revolucionario ha llegado!!!\n" +
                "\t\t</h2>\n" +
                "\t</div>\n" +
                "\t\n" +
                "</body>";

        //String html = "<body lang=PT-BR style='tab-interval:35.4pt'><img src='http://nxcache.nexon.net/all/v1.5.2/img/gnt/games-dropdown/maplestory.jpg'></body>";

        try {
            writeToFile(html,userid);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //final Html2Image html2Image = Html2Image.fromHtml(html);
        //html2Image.getImageRenderer().setHeight(1400).setWidth(800).saveImage(""+userid+".png");
        //html2Image.getHtmlImageMap().saveImageMapDocument(""+userid+".html", ""+userid+".png");
    }

    private static void despedida(String userid, String path, String name, String userimage) {

        String html = "<body>\n" +
                "\t<div style=\"background-image:url('file:"+path+"'); justify-content: flex-start; background-position: 0% 0%; background-size: 100px 100px; background-repeat: no-repeat; width: 63em;\n" +
                "  height: 31em;\">\n" +
                "\t\t<div style=\"justify-content: left; text-align:left; margin-left: 420px; margin-top: 20px;\">\n" +
                "\t\t\t<img style=\"vertical-align: middle; width: 200px; height: 200px; border-radius: 50%;\" src='"+ userimage +"'>\n" +
                "\t\t</div>\n" +
                "\t\n" +
                "\t\t<h1 style=\"text-align:middle;  font-size: 60px;font-family: Arial, Helvetica, sans-serif; margin-left: 350px;\">\n" +
                "\t\t\t"+name+"\n" +
                "\t\t</h1>\n" +
                "\t\t<h2 style=\"text-align:middle; font-size: 30px; font-family: Arial, Helvetica, sans-serif; margin-left: 240px;\">\n" +
                "\t\t\tSe ha ido, Fuera perra de mierda!!!\n" +
                "\t\t</h2>\n" +
                "\t</div>\n" +
                "\t\n" +
                "</body>";
        /*
        String html = "<body>\n" +
                "\t<div style=\"background-image:url('file:"+path+"'); background-position: 0% 50%; background-size: 20% 50%; background-repeat: no-repeat;\">\n" +
                "\t\t<div style=\"justify-content: left; text-align:left; margin-left: 170px;\">\n" +
                "\t\t\t<img style=\"vertical-align: middle; width: 150px; height: 150px; border-radius: 50%;\" src='"+ userimage +"'>\n" +
                "\t\t</div>\n" +
                "\t\n" +
                "\t\t<h1 style=\"text-align:left; font-family: Arial, Helvetica, sans-serif; margin-left: 150px;\">\n" +
                "\t\t\t"+name+"\n" +
                "\t\t</h1>\n" +
                "\t\t<h2 style=\"text-align:left; font-family: Arial, Helvetica, sans-serif; margin-left: 20px;\">\n" +
                "\t\t\tSe ha ido, Fuera perra de mierda!!!\n" +
                "\t\t</h2>\n" +
                "\t</div>\n" +
                "\t\n" +
                "</body>";
        */

        //String html = "<body lang=PT-BR style='tab-interval:35.4pt'><img src='http://nxcache.nexon.net/all/v1.5.2/img/gnt/games-dropdown/maplestory.jpg'></body>";

        try {
            writeToFile(html,userid);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Html2Image html2Image = Html2Image.fromHtml(html);
        //html2Image.getImageRenderer().setHeight(1000).setWidth(500).saveImage(userid+".png");
        //html2Image.getHtmlImageMap().saveImageMapDocument(userid+".html", userid+".png");
    }

    private static void writeToFile(String html, String name) throws IOException {
        JLabel label = new JLabel(html);
        label.setSize(200, 120);

        BufferedImage image = new BufferedImage(
                label.getWidth(), label.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        {
            // paint the html to an image
            Graphics g = image.getGraphics();
            g.setColor(Color.BLACK);
            label.paint(g);
            g.dispose();
        }

        // get the byte array of the image (as jpeg)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", new File("temp",name+".png"));
    }

    
    // make sure people aren't adding clones to dbots
    private void credit(JDA jda)
    {
        //Guild dbots = jda.getGuildById(110373943822540800L);
        //if(dbots==null)
        //    return;
        //if(bot.getConfig().getDBots())
        //    return;
        //jda.getTextChannelById(119222314964353025L)
        //        .sendMessage("This account is running JMusicBot. Please do not list bot clones on this server, <@"+bot.getConfig().getOwnerId()+">.").complete();
        //dbots.leave().queue();
    }
}
