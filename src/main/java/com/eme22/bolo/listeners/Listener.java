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
package com.eme22.bolo.listeners;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.configuration.BotConfiguration;
import com.eme22.bolo.model.MusicArtWork;
import com.eme22.bolo.model.RoleManager;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@SuppressWarnings("ConstantConditions")
@Component
@Log4j2
public class Listener extends ListenerAdapter {
    private final Bot bot;

    @Value("${config.update}")
    private boolean updatealerts;

    @Value("${config.owner}")
    private long owner;

    private final String version;

    @Autowired
    public Listener(Bot bot,  BuildProperties buildProperties) {
        this.bot = bot;
        this.version = buildProperties.getVersion();
    }

    private String setupMessage = null;

    private final HashMap<String, Integer> tempChannels = new HashMap<>();

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getGuildCache().isEmpty()) {

            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().getInviteUrl(BotConfiguration.RECOMMENDED_PERMS));
        }
        // credit(event.getJDA());
        event.getJDA().getGuilds().forEach((guild) -> {
            try {
                String defpl = bot.getSettingsManager().getSettings(guild).getDefaultPlaylist();
                VoiceChannel vc = guild.getVoiceChannelById(bot.getSettingsManager().getSettings(guild).getVoiceChannelId());
                if (defpl != null && vc != null && bot.getPlayerManager().setUpHandler(guild).playFromDefault()) {
                    guild.getAudioManager().openAudioConnection(vc);
                }
                bot.getBirthdayManager().setupBirthdays(guild);
            } catch (Exception ignore) {
            }
        });
        if (updatealerts) {
            bot.getThreadpool().scheduleWithFixedDelay(() -> {
                try {
                    User owner2 = bot.getJDA().retrieveUserById(owner).complete();
                    String latestVersion = OtherUtil.getLatestVersion();
                    if (OtherUtil.compare(version, latestVersion) < 0) {
                        String msg = String.format(OtherUtil.NEW_VERSION_AVAILABLE, version, latestVersion);
                        owner2.openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
                    }
                } catch (Exception ignored) {
                } // ignored
            }, 0, 24, TimeUnit.HOURS);
        }
    }

    @Override
    public void onMessageEmbed(@NotNull MessageEmbedEvent event) {

        if (!event.isFromGuild())
            return;

        List<Long> bannedTextChannels = bot.getSettingsManager().getSettings(event.getGuild())
                .getImageOnlyChannelsIds();

        if (bannedTextChannels.contains(event.getChannel().getIdLong())) {
            List<MessageEmbed> message = event.getMessageEmbeds();

            AtomicBoolean deletable = new AtomicBoolean(true);

            message.forEach(messageEmbed -> {
                if (messageEmbed.getImage() != null || messageEmbed.getVideoInfo() != null)
                    deletable.set(false);
            });

            if (deletable.get())
                event.getChannel().deleteMessageById(event.getMessageId()).complete();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        if (!event.isFromGuild())
            return;

        Server s = bot.getSettingsManager().getSettings(event.getGuild());

        List<Long> bannedTextChannels = s.getImageOnlyChannelsIds();

        if (bannedTextChannels.contains(event.getChannel().getIdLong())) {
            Message message = event.getMessage();

            if (message.getContentRaw().contains("delimagechannel"))
                return;

            if (message.getContentRaw().contains("https://"))
                return;

            if (message.getAttachments().isEmpty())
                message.delete().complete();

        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {

        if (!event.isFromGuild())
            return;

        bot.getNowPlayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());

    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        bot.getAloneInVoiceHandler().onVoiceUpdate(event);

        if (event.getChannelLeft() != null && event.getEntity().getUser().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            Guild guild = event.getGuild();
            ((AudioHandler) guild.getAudioManager().getSendingHandler()).stopAndClear();
            guild.getAudioManager().closeAudioConnection();
        }
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        bot.shutdown();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription("¿Desea configurar el bot?");
        event.getGuild().getDefaultChannel().asTextChannel().sendMessageEmbeds(embedBuilder.build()).queue(message -> {
            setupMessage = message.getId();
            message.addReaction(Emoji.fromFormatted("U+2705")).queue();
            message.addReaction(Emoji.fromFormatted("U+274C")).queue();
        });

    }

    private void setupDefaultChannels(Guild guild) {
        try {
            Server s = bot.getSettingsManager().getSettings(guild);
            Long commandsChannel = s.getTextChannelId();
            Long bienvenidasChannel = s.getBienvenidasChannelId();
            Long despedidasChannel = s.getDespedidasChannelId();
            TextChannel defaultChannel = guild.getDefaultChannel().asTextChannel();
            List<TextChannel> channels = guild.getTextChannels();

            if (commandsChannel == null) {
                setupChannel("Comandos de Musica", defaultChannel, channels, 0);
            }
            if (bienvenidasChannel == null) {
                setupChannel("Bienvenidas", defaultChannel, channels, 1);
            }
            if (despedidasChannel == null) {
                setupChannel("Despedidas", defaultChannel, channels, 2);
            }
        } catch (Exception exception) {

            log.error("Error: " + exception.getMessage(), exception);
        }
    }

    private void setupChannel(String title, TextChannel defaultChannel, List<TextChannel> channels, int channel) {
        ArrayList<MessageCreateData> pages = new ArrayList<>();
        int calculatedPages = (int) Math.ceil((double) channels.size() / 10);
        for (int i = 1; i <= calculatedPages; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("Seleccione el canal para ").append(title).append(": \n");
            for (int j = (i - 1) * 10; j < Math.min(i * 10, channels.size()); j++) {
                sb.append(OtherUtil.numtoString(j)).append(" ").append(channels.get(j).getName()).append("\n");
            }
            MessageCreateBuilder msb = new MessageCreateBuilder();
            msb.setContent(sb.toString());
            pages.add(msb.build());
        }

        pages.forEach(page -> defaultChannel.sendMessage(page).queue(success -> {

            tempChannels.put(success.getId(), channel);
            for (int i = 0; i < getMessageItems(page); i++) {
                success.addReaction(Emoji.fromFormatted("U+003" + i + " U+FE0F U+20E3")).queue();
            }
        }));

    }

    private int getMessageItems(MessageData message) {
        String[] chans = message.getContent().split("\n");
        chans = Arrays.copyOfRange(chans, 1, chans.length);
        return chans.length;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        if (!event.isFromGuild())
            return;

        if (setupMessage != null && setupMessage.equals(event.getMessageId())) {
            if (event.getReaction().getEmoji().getName().contains("white_check_mark")) {
                setupDefaultChannels(event.getGuild());
                return;
            }

            event.retrieveMessage().complete().delete().complete();
        }

        if (tempChannels.containsKey(event.getMessageId())) {

            String reaction = event.getReaction().getEmoji().getName();
            int channel = Integer.parseInt(reaction.replaceAll("[^\\d.]", ""));
            TextChannel channelId = getChannelFromMessage(channel, event.retrieveMessage().complete());
            if (channelId != null) {

                int mode = tempChannels.get(event.getMessageId());
                if (mode == 0) {
                    for (String key : getKeys(tempChannels, 0)) {

                        Message msgToDelete = event.getChannel().asTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild()).setTextChannelId(channelId.getIdLong());
                }
                if (mode == 1) {
                    for (String key : getKeys(tempChannels, 1)) {

                        Message msgToDelete = event.getChannel().asTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild())
                            .setBienvenidasChannelId(channelId.getIdLong());
                }
                if (mode == 2) {
                    for (String key : getKeys(tempChannels, 2)) {

                        Message msgToDelete = event.getChannel().asTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild())
                            .setDespedidasChannelId(channelId.getIdLong());
                }
            }
            return;
        }

        RoleManager manager = bot.getSettingsManager().getSettings(event.getGuild().getIdLong())
                .getRoleManager(event.getMessageIdLong());

        if (manager != null) {
            String reaction = event.getReaction().getEmoji().getAsReactionCode();

            //System.out.println(manager.isToggled());

            if (manager.isToggled()) {
                List<MessageReaction>  reactionsList = event.getChannel().asTextChannel().retrieveMessageById(event.getMessageId()).complete().getReactions();

                reactionsList.forEach(messageReaction -> {
                    List<User> users = messageReaction.retrieveUsers().complete();
                    users.forEach(user -> {

                        if (user.equals(event.getUser()) && !event.getReaction().getEmoji().equals(messageReaction.getEmoji())) {
                            messageReaction.removeReaction(user).complete();
                        }
                    });
                });

            }

            Map<String, String> data = manager.getEmoji();

            if (data.containsKey(event.getReaction().getEmoji().getAsReactionCode())) {
                String roleT = data.get(reaction);
                List<Role> list = FinderUtil.findRoles(roleT, event.getGuild());
                event.getGuild().addRoleToMember(event.getMember(), list.get(0)).queue();
            }

        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getUser().isBot())
            return;

        if (!event.isFromGuild())
            return;

        RoleManager manager = bot.getSettingsManager().getSettings(event.getGuild().getIdLong())
                .getRoleManager(event.getMessageIdLong());

        if (manager != null) {
            String reaction = event.getReaction().getEmoji().getAsReactionCode();
            Map<String, String> datas = manager.getEmoji();

            if (datas.containsKey(reaction)) {
                List<Role> list = FinderUtil.findRoles(datas.get(reaction), event.getGuild());
                event.getGuild().removeRoleFromMember(event.getMember(), list.get(0)).complete();
            }
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
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();

        switch (id) {
            case "acceptArtWork": {

                List<MessageEmbed> embeds = event.getMessage().getEmbeds();

                MusicArtWork artWork = new MusicArtWork();

                artWork.setArtist(embeds.get(0).getDescription());

                artWork.setUrl(embeds.get(1).getDescription());

                artWork.setSubmitedBy(event.getUser().getIdLong());

                bot.getArtworkImageService().addArtWork(artWork);

                event.reply("Se ha agregado correctamente la imagen al bot.").queue( event2 -> event.editButton(event.getButton().asDisabled()).queue());

                break;
            }
            case "rejectArtWork": {

                event.reply("Realizado.").queue( message -> event.getMessage().delete().queue( event2 -> event.editButton(event.getButton().asDisabled()).queue()));

                break;
            }
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        User member = event.getMember().getUser();
        try {

            //System.out.println(bot.getSettingsManager().getSettings(guild).getBienvenidasChannelEnabled());

            if (!bot.getSettingsManager().getSettings(guild).getBienvenidasChannelEnabled())
                return;

            TextChannel bienvenidas = guild.getTextChannelById(bot.getSettingsManager().getSettings(guild).getBienvenidasChannelId());

            if (bienvenidas != null) {
                InputStream bienvenida = OtherUtil.getBackground(bot.getSettingsManager().getSettings(guild), true);
                String userImage = getUserImage(member);
                File converted = getMemberFile(member);

                OtherUtil.createImage("BIENVENIDO", member.getName(), member.getId(), bienvenida, userImage, converted);
                if (!converted.exists()) {

                    log.error("Image not created");
                }

                String message = OtherUtil.getMessage(bot, guild, true);

                if (member.isBot())
                    message = "Un bot ha llegado";

                message = message.replaceAll("@username", member.getAsMention()).replaceAll("@servername",
                        guild.getName());

                // builder.setThumbnail("attachment://bienvenida.png");
                bienvenidas.sendMessage(message).addFiles(FileUpload.fromData(converted)).queue(sucess -> {
                    if (converted.delete()) {

                        log.error("Image deleted from memory after succes sended");
                    }
                });

            }
        } catch (Exception exception) {

            log.error("Error: " + exception.getMessage(), exception);
        }
    }

    private String getUserImage(User member) {
        String userImage = member.getAvatarUrl();
        if (userImage == null)
            userImage = member.getDefaultAvatarUrl();
        else
            userImage = member.getAvatarUrl();
        return userImage;
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        User member = event.getMember().getUser();

        try {

            //System.out.println(bot.getSettingsManager().getSettings(guild).getDespedidasChannelEnabled());

            if (!bot.getSettingsManager().getSettings(guild).getDespedidasChannelEnabled())
                return;


            TextChannel despedidas = guild.getTextChannelById(bot.getSettingsManager().getSettings(guild).getDespedidasChannelId());
            if (despedidas != null) {
                InputStream despedida = OtherUtil.getBackground(bot.getSettingsManager().getSettings(guild), false);

                String userImage = getUserImage(member);

                File converted = getMemberFile(member);

                OtherUtil.createImage("SE VA", member.getName(), member.getId(), despedida, userImage, converted);
                if (!converted.exists()) {

                    log.error("Image not created");
                }

                String message = OtherUtil.getMessage(bot, guild, false);
                message = message.replaceAll("@username", member.getAsMention()).replaceAll("@servername",
                        guild.getName());
                despedidas.sendMessage(message).addFiles(FileUpload.fromData(converted)).queue(sucess -> {
                    if (converted.delete()) {
                        log.error("Image deleted from memory after succes sended");
                    }
                });

            }
        } catch (Exception exception) {
            log.error("Error: " + exception.getMessage(), exception);
        }
    }

    @NotNull
    private File getMemberFile(User member) {

        File parent = new File("temp");
        if (!parent.exists()) {
            if (parent.mkdirs()) {
                log.error("Temp folder successfully created");
            }
        }

        File converted = new File(parent, member.getId() + ".png");
        if (converted.delete()) {
            log.error("Image deleted from memory before new image");
        }
        return converted;
    }

}
