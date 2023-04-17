package com.eme22.bolo.commands.general;

import com.eme22.anime.AnimeImageClient;
import com.eme22.anime.Endpoints;
import com.eme22.bolo.Bot;
import com.eme22.bolo.nsfw.NSFWStrings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class LickCmd extends ActionsCmd {

    @Value("${config.aliases.lick:}")
    String[] aliases = new String[0];

    public LickCmd() {
        super("lamer");
        this.name = "lick";
    }

    @Override
    protected String getActionDescription() {
        return NSFWStrings.getRandomLick();
    }

    @Override
    protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.WAIFU_SFW.LICK);
    }

    @Override
    protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_SFW.LICK);
    }

    @Override
    protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.HM_SFW.LICK);
    }
}
