package com.eme22.bolo.commands.general.nsfw;

import com.eme22.anime.AnimeImageClient;
import com.eme22.anime.Endpoints;
import com.eme22.bolo.commands.general.ActionsCmd;
import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class AnalCmd extends ActionsCmd {

    @Value("${config.aliases.anal:}")
    String[] aliases = new String[0];

    public AnalCmd(@Value("${config.aliases.anal:}") String[] aliases, StatsService statsService) {
        super("anal",aliases, statsService);
        this.success = new Consumer<>(){
            @Override
            public void accept(InteractionHook success) {
                super.accept(success);
                statsService.updateAnals(success.getInteraction().getGuild().getIdLong());
            }
        };
        this.success1 = new Consumer<>() {
            @Override
            public void accept(Message success) {
                super.accept(success);
                statsService.updateAnals(success.getGuild().getIdLong());
            }
        };
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
