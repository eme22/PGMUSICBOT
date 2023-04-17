package com.eme22.bolo.model;

import com.eme22.bolo.settings.SettingsManager;
import com.eme22.bolo.utils.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import lombok.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "EMBOT_SERVER_CONFIG")
public class Server implements GuildSettingsProvider {

    @JsonIgnore
    @Transient
    private SettingsManager manager;

    @Id
    @Column(name = "SERVER_ID", nullable = false)
    private Long id;

    @Column(name = "SERVER_MUSICTEXTCHANEL")
    private long textChannelId;
    @Column(name = "SERVER_MUSICVOICECHANEL")
    private long voiceChannelId;

    @Column(name = "SERVER_DJROLE")
    private long djRoleId;

    @Column(name = "SERVER_ADMINROLE")
    private long adminRoleId;

    @Column(name = "SERVER_MUSICVOLUME")
    private int volume = Constants.DEFAULT_VOLUME;

    @Column(name = "SERVER_DEFAULTPLAYLISTNAME")
    private String defaultPlaylist;

    @Column(name = "SERVER_REPEATMODE")
    @Enumerated(EnumType.STRING)
    private RepeatMode repeatMode;

    @Column(name = "SERVER_PREFIX")
    private String prefix;

    @Column(name = "SERVER_MUSICSKIPRATIO")
    private double skipRatio;

    @Column(name = "SERVER_WELCOMECHANNELENABLED")
    private Boolean bienvenidasChannelEnabled = false;

    @Column(name = "SERVER_WELCOMECHANNELID")
    private long bienvenidasChannelId;

    @Column(name = "SERVER_WELCOMECHANNELIMAGE")
    private String bienvenidasChannelImage;

    @Column(name = "SERVER_WELCOMECHANNELMESSAGE")
    private String bienvenidasChannelMessage;

    @Column(name = "SERVER_GOODBYECHANNELENABLED")
    private Boolean despedidasChannelEnabled = false;

    @Column(name = "SERVER_GOODBYECHANNELID")
    private long despedidasChannelId;

    @Column(name = "SERVER_GOODBYECHANNELIMAGE")
    private String despedidasChannelImage;

    @Column(name = "SERVER_GOODBYECHANNELMESSAGE")
    private String despedidasChannelMessage;

    @ElementCollection
    @CollectionTable(name="EMBOT_SERVER_IMAGECHANNELS")
    private List<Long> imageOnlyChannelsIds = new ArrayList<>();

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "EMBOT_SERVER_MEMEIMAGE",
            joinColumns = @JoinColumn(name = "MEMEIMAGELIST_ID"),
            inverseJoinColumns = @JoinColumn(name = "MEME_ID"))
    @ToString.Exclude
    private List<MemeImage> memeImages = new ArrayList<>();

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "EMBOT_SERVER_POLL",
            joinColumns = @JoinColumn(name = "POLLLIST_ID"),
            inverseJoinColumns = @JoinColumn(name = "POLL_ID"))
    @ToString.Exclude
    private List<Poll> polls = new ArrayList<>();

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "EMBOT_SERVER_ROLEMANAGER",
            joinColumns = @JoinColumn(name = "ROLEMANAGERLIST_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    @ToString.Exclude
    private List<RoleManager> roleManagerList = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name="EMBOT_SERVER_8BALLRESPONSES")
    private List<String> eightBallAnswers = new ArrayList<>();

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "EMBOT_SERVER_BIRTHDAY",
            joinColumns = @JoinColumn(name = "BIRTHDAYLIST_ID"),
            inverseJoinColumns = @JoinColumn(name = "BIRTHDAY_ID"))
    @ToString.Exclude
    private List<Birthday> birthdays = new ArrayList<>();

    @Column(name = "SERVER_BIRTHDAYCHANNELID")
    private long birthdayChannelId;
    @Column(name = "SERVER_ANTIRAIDMODE")
    private Boolean antiRaidMode = false;

    //STATS

    @Column(name = "SERVER_SONGSSEND")
    private int songsSend;
    @Column(name = "SERVER_USEDTIMES")
    private int usedTimes;

    @Column(name = "SERVER_COMMANDSSEND")
    private int commandsSend;

    @Column(name = "SERVER_DATEADDED")
    private Date dateAdded;

    @Column(name = "SERVER_DATEDISMISSED")
    private Date dateDismissed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Server server = (Server) o;
        return getId() != null && Objects.equals(getId(), server.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void removePollFromGuild(long messageIdLong) {
        polls.removeIf(poll -> poll.getId().equals(messageIdLong));
    }

    public void deleteRoleManagers(long messageIdLong) {
        roleManagerList.removeIf(role -> role.getId().equals(messageIdLong));
    }

    public void addToEightBallAnswers(String answer) {
        eightBallAnswers.add(answer);
    }

    public void addOnlyImageChannels(TextChannel textChannel) {
        imageOnlyChannelsIds.add(textChannel.getIdLong());
    }

    public void addOnlyImageChannels(long textChannelId) {
        imageOnlyChannelsIds.add(textChannelId);
    }

    public void addToMemeImages(String message, String link) {
        memeImages.add(new MemeImage(0L ,message, link));
    }

    public void removeFrom8BallAnswers(int answer) {
        eightBallAnswers.remove(answer);
    }

    public boolean isOnlyImageChannel(TextChannel textChannel) {
        return isOnlyImageChannel(textChannel.getIdLong());
    }

    public boolean isOnlyImageChannel(long textChannelId) {
        return imageOnlyChannelsIds.contains(textChannelId);
    }

    public void removeFromOnlyImageChannels(TextChannel textChannel) {
        removeFromOnlyImageChannels(textChannel.getIdLong());
    }

    public void removeFromOnlyImageChannels(long textChannelId) {
        imageOnlyChannelsIds.remove(textChannelId);
    }

    public void addPoll(Poll poll) {
        polls.add(poll);
    }

    public void save() {
        manager.saveSettings(this);
    }

    public void deleteFromMemeImages(int i) {
        memeImages.remove(i);
    }

    public void addToRoleManagers(RoleManager manager) {
        roleManagerList.add(manager);
    }

    public RoleManager getRoleManager(long messageIdLong) {
        return roleManagerList.stream().filter(customer -> customer.getId().equals(messageIdLong))
                .findAny()
                .orElse(null);
    }

    public String getRandomAnswer() {
        return eightBallAnswers.get(new Random().nextInt(eightBallAnswers.size()));
    }

    public MemeImage getRandomMemeImages() {
        return memeImages.get(new Random().nextInt(memeImages.size()));
    }

    public void removeBirthDay(long idLong) {
        birthdays.removeIf( bd -> bd.getId().equals(idLong));
    }

    public void addBirthDay(Birthday cumple) {
        birthdays.add(cumple);
    }

    public Birthday getUserBirthday(long idLong) {
        return birthdays.stream().filter(user -> user.getUser().equals(idLong)).findAny().orElse(null);
    }
    @Nullable
    @Override
    public Collection<String> getPrefixes() {
        return Collections.singletonList(prefix);
    }
}
