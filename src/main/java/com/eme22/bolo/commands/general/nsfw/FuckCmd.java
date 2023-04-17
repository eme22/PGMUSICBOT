package com.eme22.bolo.commands.general.nsfw;

import com.eme22.anime.AnimeImageClient;
import com.eme22.anime.Endpoints;
import com.eme22.bolo.commands.general.ActionsCmd;
import com.eme22.bolo.nsfw.NSFWStrings;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class FuckCmd extends ActionsCmd {

    @Value("${config.aliases.fuck:}")
    String[] aliases = new String[0];

    public FuckCmd() {
        super("fuck");
        this.name = "fuck";
    }

    @Override
    protected String getActionDescription() {
        return NSFWStrings.getRandomFuck();
    }

    @Override
    protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.HM_NSFW.GANGBANG);
    }

    @Override
    protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_NSFW.FUCK);
    }

    @Override
    protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_NSFW.FUCK);
    }
}
