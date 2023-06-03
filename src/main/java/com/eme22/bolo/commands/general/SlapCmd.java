package com.eme22.bolo.commands.general;

import com.eme22.anime.AnimeImageClient;
import com.eme22.anime.Endpoints;
import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class SlapCmd extends ActionsCmd {

    public SlapCmd(@Value("${config.aliases.slap:}") String[] aliases, StatsService statsService) {
        super("cachetear",aliases, statsService);
        this.name = "slap";
        this.success = new Consumer<>(){
            @Override
            public void accept(InteractionHook success) {
                super.accept(success);
                statsService.updateSlaps(success.getInteraction().getGuild().getIdLong());
            }
        };
        this.success1 = new Consumer<>() {
            @Override
            public void accept(Message success) {
                super.accept(success);
                statsService.updateSlaps(success.getGuild().getIdLong());
            }
        };
    }

    @Override
    protected String getActionDescription() {
        return NSFWStrings.getRandomSlap();
    }

    @Override
    protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.WAIFU_SFW.SLAP);
    }

    @Override
    protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.KAWAII_SFW.SLAP);
    }

    @Override
    protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
        return animeImageClient.getImage(Endpoints.NEKO.SLAP);
    }
}
