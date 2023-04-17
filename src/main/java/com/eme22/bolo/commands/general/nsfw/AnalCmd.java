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
public class AnalCmd extends ActionsCmd {

    @Value("${config.aliases.anal:}")
    String[] aliases = new String[0];

    public AnalCmd() {
        super("anal");
        this.name = "anal";
    }

    @Override
    protected String getActionDescription() {
        return NSFWStrings.getRandomAnal();
    }

    @Override
    protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_NSFW.ANAL);
    }

    @Override
    protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_NSFW.ANAL);
    }

    @Override
    protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_NSFW.ANAL);
    }
}
