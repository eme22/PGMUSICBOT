package com.eme22.bolo.commands.general;

import com.eme22.anime.AnimeImageClient;
import com.eme22.anime.Endpoints;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Log4j2
public abstract class ActionsCmd extends BaseCommand {

    private static int maxRetries = 3;
    private int retries = 0;

    protected final StatsService statsService;
    protected Consumer<InteractionHook> success = new Consumer<>();
    protected Consumer<Message> success1 = new Consumer<>();

    public ActionsCmd(String name2, StatsService statsService) {
        this.name = name2;
        this.help = name2+" al usuario seleccionado";
        this.arguments = "<user>";
        this.guildOnly = true;
        this.statsService = statsService;
        this.options = Collections.singletonList(
                new OptionData(OptionType.USER, "usuario", "busca el usuario a "+name2+".").setRequired(true));
    }

    public ActionsCmd(String name2, String[] aliases,StatsService statsService) {
        this.name = name2;
        this.help = name2+" al usuario seleccionado";
        this.aliases = aliases;
        this.arguments = "<user>";
        this.guildOnly = true;
        this.statsService = statsService;
        this.options = Collections.singletonList(
                new OptionData(OptionType.USER, "usuario", "busca el usuario a "+name2+".").setRequired(true));
    }

    protected abstract String getActionDescription();
    protected abstract String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException;

    protected abstract String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException;

    protected abstract String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException;

    @Override
    protected void execute(SlashCommandEvent event) {
        Member memberKisser = event.getMember();
        Member memberKissed = event.getOption("usuario").getAsMember();

        if (memberKissed.getUser().isBot()) {
            event.reply(event.getClient().getError() + " Asegurese de que el usuario no sea un bot").setEphemeral(true)
                    .queue();
            return;
        }
        if (memberKisser.equals(memberKissed)) {
            event.reply(event.getClient().getError() + "Asegurese de que el usuario no sea usted").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(memberKisser.getAsMention() + getActionDescription() + memberKissed.getAsMention());
        builder.setImage(getRandomImage());
        event.replyEmbeds(builder.build()).queue(success);
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Por favor incluya un nombre");
            return;
        }

        List<Member> member = FinderUtil.findMembers(event.getArgs(), event.getGuild());

        if (member.isEmpty()) {
            event.replyError("Asegurese de que el usuario exista y no sea un bot");
            return;
        }

        Member memberKisser = event.getMember();
        Member memberKissed = member.get(0);

        if (memberKisser.equals(memberKissed)) {
            event.replyError("Asegurese de que el usuario no sea usted");
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(memberKisser.getAsMention() + getActionDescription() + memberKissed.getAsMention());
        builder.setImage(getRandomImage());
        event.reply(builder.build(), success1);
    }

    private String getRandomImage() {
        AnimeImageClient animeImageClient = new AnimeImageClient();
        try {
            if (new Random().nextBoolean())
                if (new Random().nextBoolean())
                    return loadActionImageUrl1(animeImageClient);
                else
                    return loadActionImageUrl3(animeImageClient);
            else
                if (new Random().nextBoolean())
                    return loadActionImageUrl2(animeImageClient);
                else
                    return loadActionImageUrl3(animeImageClient);
        }
        catch (Exception e) {
            log.warn("No se ha podido obtener una imagen", e);
            if (retries <= maxRetries){
                retries++;
                return getRandomImage();
            }

            retries = 0;
            return null;
        }
    }

    public class Consumer<T> implements java.util.function.Consumer<T> {
        @Override
        public void accept(T success) {

            if (success instanceof Message)
                statsService.updateImagesSend(((Message) success).getGuild().getIdLong());
            else if (success instanceof InteractionHook)
                statsService.updateImagesSend(((InteractionHook) success).getInteraction().getGuild().getIdLong());
        }
    }


}
