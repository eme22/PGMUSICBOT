package com.eme22.bolo.commands.general;

import com.eme22.anime.AnimeImageClient;
import com.eme22.anime.Endpoints;
import com.eme22.bolo.nsfw.NSFWStrings;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class BiteCmd extends ActionsCmd{

    @Value("${config.aliases.bite:}")
    String[] aliases = new String[0];

    public BiteCmd() {
        super("muerde");
        this.name = "bite";
    }

    @Override
    protected String getActionDescription() {
        return NSFWStrings.getRandomBite();
    }

    @Override
    protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.WAIFU_SFW.BITE);
    }

    @Override
    protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_SFW.BITE);
    }

    @Override
    protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.WAIFU_SFW.BITE);
    }
}
