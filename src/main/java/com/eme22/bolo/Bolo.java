/*
 * Copyright 2016 John Grosh (jagrosh).
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
package com.eme22.bolo;

import com.eme22.bolo.commands.admin.*;
import com.eme22.bolo.commands.dj.*;
import com.eme22.bolo.commands.general.*;
import com.eme22.bolo.commands.music.*;
import com.eme22.bolo.commands.owner.*;
import com.eme22.bolo.entities.Prompt;
import com.eme22.bolo.gui.GUI;
import com.eme22.bolo.settings.SettingsManager;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.NotFoundHttpHandler;
import org.webbitserver.handler.StaticFileHandler;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class Bolo
{
    public final static String PLAY_EMOJI  = "\u25B6"; // ▶
    public final static String PAUSE_EMOJI = "\u23F8"; // ⏸
    public final static String STOP_EMOJI  = "\u23F9"; // ⏹
    public final static Permission[] RECOMMENDED_PERMS = {Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
                                Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.MESSAGE_EXT_EMOJI,
                                Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE};
    public final static GatewayIntent[] INTENTS = {GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS};
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // startup log
        Logger log = LoggerFactory.getLogger("Startup");

        waitExec(log, args);
        startSocket();

        // create prompt to handle startup
        Prompt prompt = new Prompt("JMusicBot", "Switching to nogui mode. You can manually start in nogui mode by including the -Dnogui=true flag.");
        
        // get and check latest version
        String version = OtherUtil.checkVersion(prompt);

        // create settings
        SettingsManager settings = new SettingsManager();

        try {
            OtherUtil.loadFileFromGit(new File("serversettings.json"));
        } catch (IOException | NoSuchAlgorithmException | NullPointerException e) {
            LoggerFactory.getLogger("Settings").warn("Se ha fallado en cargar las opciones del servidor, se usaran las locales: "+e);

        }

        try {
            OtherUtil.loadFileFromGit(new File("serverpolls.json"));
        }catch (IOException | NullPointerException | NoSuchAlgorithmException e){
            LoggerFactory.getLogger("Settings").warn("Se ha fallado en cargar los datos de votaciones del servidor: "+e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                settings.writeSettings();
                OtherUtil.writeFileToGitHub(new File("serversettings.json"));
            } catch (IOException | NoSuchAlgorithmException | NullPointerException e) {
                e.printStackTrace();
            }
        }));

        // check for valid java version
        if(!System.getProperty("java.vm.name").contains("64"))
            prompt.alert(Prompt.Level.WARNING, "Java Version", "It appears that you may not be using a supported Java version. Please use 64-bit java.");
        
        // load config
        BotConfig config = new BotConfig(prompt);
        config.load();
        if(!config.isValid())
            return;
        
        // set up the listener
        EventWaiter waiter = new EventWaiter();
        Bot bot = new Bot(waiter, config, settings);
        
        AboutCommand aboutCommand = new AboutCommand(Color.BLUE.brighter(),
                                "Hola soy Bolo' un BOT con lag) (v"+version+") [Status: https://bolo2022.herokuapp.com/]",
                                new String[]{"Musica en HD", "Mensaje de bienvenida y despedida", "Limpiar mensajes"},
                                RECOMMENDED_PERMS);
        aboutCommand.setIsAuthor(false);
        aboutCommand.setReplacementCharacter("\uD83C\uDFB6"); // 🎶
        
        // set up the command client
        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefix(config.getPrefix())
                .setAlternativePrefix(config.getAltPrefix())
                .setOwnerId(Long.toString(config.getOwnerId()))
                .setEmojis(config.getSuccess(), config.getWarning(), config.getError())
                .setHelpWord(config.getHelp())
                .setLinkedCacheSize(200)
                .setGuildSettingsManager(settings)
                .addCommands(aboutCommand,
                        new PingCommand(),
                        new SettingsCmd(bot),
                        new AvatarCmd(bot),
                        new MemeCmd(bot),
                        new MemeListCmd(bot),
                        new ShowImageChannelsCmd(bot),

                        new LyricsCmd(bot),
                        new NowplayingCmd(bot),
                        new PlayCmd(bot),
                        new PlaylistsCmd(bot),
                        new QueueCmd(bot),
                        new RemoveCmd(bot),
                        new SearchCmd(bot),
                        new SCSearchCmd(bot),
                        new ShuffleCmd(bot),
                        new SkipCmd(bot),

                        new ForceRemoveCmd(bot),
                        new ForceskipCmd(bot),
                        new MoveTrackCmd(bot),
                        new PauseCmd(bot),
                        new PlaynextCmd(bot),
                        new RepeatCmd(bot),
                        new SkiptoCmd(bot),
                        new StopCmd(bot),
                        new VolumeCmd(bot),

                        new PrefixCmd(bot),
                        new SetdjCmd(bot),
                        new PollCmd(bot),
                        new AddMemeCmd(bot),
                        new RemoveMemeCmd(bot),
                        new AddImageChannel(bot),
                        new DeleteImageChannel(bot),
                        new SetWelcomeCmd(bot),
                        new SetGoodByeCmd(bot),
                        new CloneChannelCmd(bot),
                        new CloneAndDeleteChannel(bot),
                        new SetAdminCmd(bot),
                        new SkipratioCmd(bot),
                        new SettcCmd(bot),
                        new SetvcCmd(bot),
                        new ClearDataCmd(bot),
                        new ClearMessagesCmd(bot),
                        
                        new AutoplaylistCmd(bot),
                        new DebugCmd(bot),
                        new PlaylistCmd(bot),
                        new SetavatarCmd(bot),
                        new SetgameCmd(bot),
                        new SetnameCmd(bot),
                        new SetstatusCmd(bot),
                        new ShutdownCmd(bot)
                );
        if(config.useEval())
            cb.addCommand(new EvalCmd(bot));
        boolean nogame = false;
        if(config.getStatus()!=OnlineStatus.UNKNOWN)
            cb.setStatus(config.getStatus());
        if(config.getGame()==null)
            cb.useDefaultGame();
        else if(config.getGame().getName().equalsIgnoreCase("none"))
        {
            cb.setActivity(null);
            nogame = true;
        }
        else
            cb.setActivity(config.getGame());
        
        if(!prompt.isNoGUI())
        {
            try 
            {
                GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            } 
            catch(Exception e) 
            {
                log.error("Could not start GUI. If you are "
                        + "running on a server or in a location where you cannot display a "
                        + "window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }
        
        log.info("Loaded config from " + config.getConfigLocation());
        
        // attempt to log in and start
        try
        {
            JDA jda = JDABuilder.create(config.getToken(), Arrays.asList(INTENTS))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE, CacheFlag.ONLINE_STATUS)
                    .setActivity(nogame ? null : Activity.playing("loading..."))
                    .setStatus(config.getStatus()==OnlineStatus.INVISIBLE || config.getStatus()==OnlineStatus.OFFLINE 
                            ? OnlineStatus.INVISIBLE : OnlineStatus.DO_NOT_DISTURB)
                    .addEventListeners(cb.build(), waiter, new Listener(bot))
                    .setBulkDeleteSplittingEnabled(true)
                    .build();
            bot.setJDA(jda);
        }
        catch (LoginException ex)
        {
            prompt.alert(Prompt.Level.ERROR, "JMusicBot", ex + "\nPlease make sure you are "
                    + "editing the correct config.txt file, and that you have used the "
                    + "correct token (not the 'secret'!)\nConfig Location: " + config.getConfigLocation());
            System.exit(1);
        }
        catch(IllegalArgumentException ex)
        {
            prompt.alert(Prompt.Level.ERROR, "JMusicBot", "Some aspect of the configuration is "
                    + "invalid: " + ex + "\nConfig Location: " + config.getConfigLocation());
            System.exit(1);
        }
    }

    private static void startSocket() {

        String port = System.getProperty("server.port");

        WebServer webServer = WebServers.createWebServer(Integer.parseInt(port))
                .add(new StaticFileHandler("."))
                .add( new webserver.NotFoundHandler())
                ;
        webServer.start();
        System.out.println("Server running at " + webServer.getUri());
    }

    private static void waitExec(Logger log, String[] args) {

        boolean wait = true;

        for (String arg : args) {
            if (arg.equalsIgnoreCase("wait")) {
                wait = false;
                break;
            }
        }

        if (wait)
            for(int j = 20; j >= 0; j--) {
                log.info(j + " seconds remaining");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }
}
