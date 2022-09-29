package com.eme22.bolo.birthday;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.Birthday;
import com.eme22.bolo.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class BirthdayManager {

    public static final Timer timer = new Timer();

    private final Bot bot;

    public BirthdayManager(Bot bot) {
        this.bot = bot;
    }

    public void setupBirthdays(Guild guild) {
        Settings settings = bot.getSettingsManager().getSettings(guild);

        settings.getBirthdays().forEach(
                birthday -> {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        TextChannel channel = guild.getTextChannelById(settings.getBirthdayChannelId());
                        if (channel == null) return;

                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setDescription(birthday.getMessage());
                        channel.sendMessageEmbeds(embedBuilder.build()).queue();
                }
            };

            timer.schedule(task, birthday.getDate());
        });

    }
}
