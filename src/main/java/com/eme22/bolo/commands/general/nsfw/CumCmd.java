package com.eme22.bolo.commands.general.nsfw;

import com.eme22.anime.AnimeImageClient;
import com.eme22.anime.Endpoints;
import com.eme22.bolo.commands.general.ActionsCmd;
import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class CumCmd extends ActionsCmd {

    public CumCmd(@Value("${config.aliases.cum:}") String[] aliases, StatsService statsService) {
        super("cum",aliases, statsService);
    }

    @Override
    protected String getActionDescription() {
        return NSFWStrings.getRandomCum();
    }

    @Override
    protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.HM_NSFW.CUM);
    }

    @Override
    protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_NSFW.CUM);
    }

    @Override
    protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_NSFW.CUM);
    }
}
