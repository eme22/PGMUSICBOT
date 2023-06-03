package com.eme22.bolo.configuration;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.owner.EvalCmd;
import com.eme22.bolo.entities.Prompt;
import com.eme22.bolo.gui.GUI;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.settings.SettingsManager;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.*;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Configuration
@Log4j2
public class BotConfiguration {

    public final static Permission[] RECOMMENDED_PERMS = { Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.ADMINISTRATOR,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE, Permission.MANAGE_WEBHOOKS };
    public final static GatewayIntent[] INTENTS = { GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_WEBHOOKS };

    public static final String message = "Hola soy MBotApplication' un BOT con lag) (v%s) ";

    private static final String DEFAULT_PREFIX = "@mention";

    public static final String[] features = new String[] {
            "Musica en HQ",
            "Mensaje de bienvenida y despedida configurables",
            "Limpiar mensajes",
            "Votaciones",
            "Memes",
            "Manejo de roles"
    };

    public final static int DEFAULT_VOLUME = 100;

    private final Prompt prompt;

    private final static String CONTEXT = "Config";
    private final static String START_TOKEN = "/// START OF JMUSICBOT CONFIG ///";
    private final static String END_TOKEN = "/// END OF JMUSICBOT CONFIG ///";

    @Value("${jda.token}")
    private String token;
    @Value("${spotify.userid}")
    private String spotifyUserId;
    @Value("${spotify.secret}")
    private String spotifySecret;
    @Value("${github.token}")
    private String githubToken;
    @Value("${config.prefix}")
    private String prefix;
    @Value("${config.altprefix}")
    private String altprefix;
    @Value("${config.help}")
    private String helpWord;
    @Value("${config.playlistsfolder}")
    private String playlistsFolder;
    @Value("${config.success}")
    private String successEmoji;
    @Value("${config.warning}")
    private String warningEmoji;
    @Value("${config.error}")
    private String errorEmoji;
    @Value("${config.loading}")
    private String loadingEmoji;
    @Value("${config.searching}")
    private String searchingEmoji;
    @Value("${config.welcome}")
    private String welcomeString;
    @Value("${config.goodbye}")
    private String goodByeString;
    @Value("${config.stayinchannel}")
    private boolean stayInChannel;
    @Value("${config.songinstatus}")
    private boolean songInStatus;
    @Value("${config.nowplayingimages}")
    private boolean npImages;
    @Value("${config.update}")
    private boolean updatealerts;
    @Value("${config.eval}")
    private boolean useEval;
    private boolean dbots;
    @Value("${config.owner}")
    private long owner;
    @Value("${config.maxseconds}")
    private long maxSeconds;
    @Value("${config.alonetimeuntilstop}")
    private long aloneTimeUntilStop;
    private OnlineStatus status;

    @Value("${config.game}")
    private String game;

    @Value("${config.discordChannel}")
    private String botDiscord;

    private boolean valid = false;

    @Value("${config.oldFile}")
    private String oldFile;

    private final Environment environment;

    private final SettingsManager settingsManager;

    @Autowired
    public BotConfiguration(Prompt prompt, Environment env, SettingsManager settingsManager) {
        this.prompt = prompt;
        this.settingsManager = settingsManager;
        this.environment = env;
    }
/**
    @ PostConstruct
    public void init() {
        if (Objects.equals(environment.getProperty("updateGit"), "true")) {

            try {
                File oldSettings = new File("serversettings.json");
                OtherUtil.loadFileFromGit(oldSettings);
                settingsManager.updateOldSettings(oldSettings);

            } catch (IOException | NoSuchAlgorithmException e) {
                log.warn("Se ha fallado en cargar las opciones del servidor", e);
            }


        }
    }
 **/
    @Bean
    JDA getJDA(CommandClientBuilder cb, Activity game, EventWaiter waiter,Bot bot, ListenerAdapter... listeners) {
        boolean nogame = false;
        if (status != OnlineStatus.UNKNOWN)
            cb.setStatus(status);
        if (game == null)
            cb.useDefaultGame();
        else if (game.getName().equalsIgnoreCase("none")) {
            cb.setActivity(null);
            nogame = true;
        } else
            cb.setActivity(game);

        if (!prompt.isNoGUI()) {
            try {
                GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            } catch (Exception e) {
                log.error("Could not start GUI. If you are "
                        + "running on a server or in a location where you cannot display a "
                        + "window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }

        JDA jda = JDABuilder.create(token, Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS)
                .setActivity(nogame ? null : Activity.playing("loading..."))
                .setStatus(
                        status == OnlineStatus.INVISIBLE || status == OnlineStatus.OFFLINE
                                ? OnlineStatus.INVISIBLE
                                : OnlineStatus.DO_NOT_DISTURB)

                .setBulkDeleteSplittingEnabled(true)
                .addEventListeners(cb.build(), waiter)
                .addEventListeners((Object[]) listeners)
                .build();


        bot.setJDA(jda);

        return jda;
    }

    @Bean
    EventWaiter getEventWaiter() {
        return new EventWaiter();
    }

    @Bean
    CommandClientBuilder getCommandClientBuilder(GuildSettingsManager<Server> setting, Activity game, Bot bot, Prompt prompt, Consumer<CommandEvent> helpbean, Command[] commands, SlashCommand[] slashCommands) {

        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefix(prefix)
                .setAlternativePrefix(altprefix)
                .setOwnerId(Long.toString(owner))
                .setEmojis(successEmoji, warningEmoji, errorEmoji)
                .setHelpWord(helpWord)
                .setHelpConsumer(helpbean)
                .setLinkedCacheSize(200)
                .setGuildSettingsManager(setting)
                .addSlashCommands(slashCommands)
                .addCommands(commands)
                .setListener(new CommandListener() {
                })
                ;

        if (status != OnlineStatus.UNKNOWN)
            cb.setStatus(status);
        if (game == null)
            cb.useDefaultGame();
        else if (game.getName().equalsIgnoreCase("none")) {
            cb.setActivity(null);
        } else
            cb.setActivity(game);

        if (!prompt.isNoGUI()) {
            try {
                GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            } catch (Exception e) {
                log.error("Could not start GUI. If you are "
                        + "running on a server or in a location where you cannot display a "
                        + "window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }

        log.info("Loaded config from Database");

        return cb;
    }

    @Bean
    Command getEvalCommand(Bot bot) {

        if (useEval)
            return new EvalCmd(bot);

        return null;
    }

    /*
    @Bean
    AboutCommand getAboutCommand(Color color) {

        String version = buildProperties.getVersion();

        AboutCommand aboutCommand = new AboutCommand(
                color,
                String.format(message, version),
                features,
                RECOMMENDED_PERMS);
        aboutCommand.setIsAuthor(false);
        aboutCommand.setReplacementCharacter("\uD83C\uDFB6"); // 🎶

        return aboutCommand;
    }
    */

    @Bean
    public Color colorBean(){
        return Color.CYAN;
    }

    @Bean
    public Activity activityBean() {
        return Activity.playing(game);
    }

    @Bean(name = "djCategory")
    public Command.Category djCategoryBean() {
        return new Command.Category("DJ", event -> {
            if(event.getAuthor().getId().equals(event.getClient().getOwnerId()))
                return true;
            if (event.getAuthor().equals(event.getGuild().getOwner().getUser()))
                return true;
            Server settings = event.getClient().getSettingsFor(event.getGuild());
            Role admin = event.getGuild().getRoleById(settings.getAdminRoleId());
            if(event.getMember().getRoles().contains(admin))
                return true;

            Role dj = event.getGuild().getRoleById(settings.getDjRoleId());
            return dj!=null && (event.getMember().getRoles().contains(dj) || dj.getIdLong()==event.getMember().getIdLong());

        });
    }

    @Bean(name = "adminCategory")
    public Command.Category adminCategoryBean() {
        return new Command.Category("Admin", event ->
        {
            if(event.getAuthor().getId().equals(event.getClient().getOwnerId()))
                return true;
            if (event.getAuthor().getId().equals(event.getGuild().getOwnerId()))
                return true;
            if(event.getGuild()==null)
                return true;
            Server settings = event.getClient().getSettingsFor(event.getGuild());
            Role admin = event.getGuild().getRoleById(settings.getAdminRoleId());
            return admin!=null && (event.getMember().getRoles().contains(admin) || admin.getIdLong()==event.getGuild().getIdLong());
        });
    }

    @Bean
    public Consumer<CommandEvent> helpBean(Command[] commands) {
        return event -> {
            StringBuilder builder = new StringBuilder("Comandos de **" + event.getSelfUser().getName() + "**\n");
            List<String> strings = new ArrayList<>();
            int preMaxSize = 1980;
            Command.Category category = null;
            for (Command command : commands) {
                if (!command.isHidden() && (!command.isOwnerCommand() || event.isOwner())) {
                    if (!Objects.equals(category, command.getCategory())) {
                        category = command.getCategory();
                        builder.append("\n\n  __").append(category == null ? "Miscelaneos" : category.getName()).append("__:\n");
                    }
                    builder.append("\n`").append(prefix.equals(DEFAULT_PREFIX) ? "@"+event.getJDA().getSelfUser().getName()+" " : prefix).append(prefix == null ? " " : "").append(command.getName())
                            .append(command.getArguments() == null ? "`" : " " + command.getArguments() + "`")
                            .append(" - ").append(command.getHelp());

                    if (builder.length() > preMaxSize) {
                        strings.add(builder.toString());
                        builder = new StringBuilder();
                    }

                }
            }

            log.error("Owner: "+ owner);

            User owner2 = event.getJDA().getUserById(owner);

            log.error("Owner2: "+ owner2);

            if (owner2 != null) {

                if (builder.length() > preMaxSize) {
                    strings.add(builder.toString());
                    builder = new StringBuilder();
                }

                builder.append("\n\nPara ayuda adicional, contacta a **").append(owner2.getName()).append("**#").append(owner2.getDiscriminator());
                if (botDiscord != null && !botDiscord.isEmpty())
                    builder.append(" o unete a ").append(botDiscord);

                strings.add(builder.toString());
            }

            for (String message: strings) {
                event.replyInDm(message, unused -> {}, t -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
            }
        };
    }

}
