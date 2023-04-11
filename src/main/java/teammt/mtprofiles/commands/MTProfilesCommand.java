package teammt.mtprofiles.commands;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import masecla.mlib.annotations.RegisterableInfo;
import masecla.mlib.annotations.SubcommandInfo;
import masecla.mlib.classes.Registerable;
import masecla.mlib.classes.Replaceable;
import masecla.mlib.main.MLib;
import teammt.mtprofiles.container.SocialMediaContainer;
import teammt.mtprofiles.data.PlayerProfile;
import teammt.mtprofiles.managers.PlatformManager;
import teammt.mtprofiles.managers.PlayerProfileManager;

@RegisterableInfo(command = "mtprofiles")
public class MTProfilesCommand extends Registerable {

    PlayerProfileManager playerProfileManager;
    PlatformManager platformManager;
    SocialMediaContainer socialMediaContainer;

    public MTProfilesCommand(MLib lib, PlayerProfileManager playerProfileManager, PlatformManager platformManager,
            SocialMediaContainer socialMediaContainer) {
        super(lib);
        this.playerProfileManager = playerProfileManager;
        this.platformManager = platformManager;
        this.socialMediaContainer = socialMediaContainer;
    }

    /**
     * Opens help menu
     */
    @SubcommandInfo(subcommand = "", permission = "teammt.mtprofiles.help")
    public void handleNoArgs(Player sender) {
        lib.getMessagesAPI().sendMessage("help-message", sender);
    }

    /**
     * Opens help menu
     */
    @SubcommandInfo(subcommand = "help", permission = "teammt.mtprofiles.help")
    public void handleHelp(Player sender) {
        lib.getMessagesAPI().sendMessage("help-message", sender);
    }

    /**
     * Reload configurations currently doing the config reload
     */
    @SubcommandInfo(subcommand = "reload", permission = "teammt.mtprofiles.reload")
    public void onReload(Player sender) {
        try {
            lib.getConfigurationAPI().reloadAll();
        } catch (IOException e) {
        }
        lib.getMessagesAPI().reloadSharedConfig();
        lib.getMessagesAPI().sendMessage("plugin-reloaded", sender);
    }

    /**
     * Opens the social media menu for the player
     */
    @SubcommandInfo(subcommand = "social", permission = "teammt.mtprofiles.social")
    public void handleSocial(Player sender) {
        playerProfileManager.setLookingSocialMedia(sender.getUniqueId(), sender.getUniqueId());
        lib.getContainerAPI().openFor(sender, SocialMediaContainer.class);
    }

    /**
     * Opens the social media menu for other players
     */
    @SubcommandInfo(subcommand = "social", permission = "teammt.mtprofiles.social")
    public void handleSocial(Player sender, String target) {
        OfflinePlayer offlinePlayer = null;
		for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
			if (op.getName().equalsIgnoreCase(target))
				offlinePlayer = op;
		}
		if (offlinePlayer == null) {
			lib.getMessagesAPI().sendMessage("player-not-found", sender, new Replaceable("%player%", target));
			return;
		}
		UUID uuid = offlinePlayer.getUniqueId();
        
        if (playerProfileManager.getPlayerProfile(uuid) == null) {
            lib.getMessagesAPI().sendMessage("no-social-profile", sender, new Replaceable("%player%", target));
            return;
        }
        playerProfileManager.setLookingSocialMedia(sender.getUniqueId(), uuid);
        lib.getContainerAPI().openFor(sender, SocialMediaContainer.class);
    }

    /**
     * Links a platform to the player
     */
    @SubcommandInfo(subcommand = "link", permission = "teammt.mtprofiles.link")
    public void handleLink(Player sender, String platform, String... tag) {
        playerProfileManager.link(sender, platform.toLowerCase(), tag[0]);
    }

    /**
     * Unlinks a platform from the player
     */
    @SubcommandInfo(subcommand = "unlink", permission = "teammt.mtprofiles.unlink")
    public void unlink(Player sender, String platform) {
        playerProfileManager.unlink(sender, platform.toLowerCase());
    }

    /**
     * Follows a person's socials
     */
    @SubcommandInfo(subcommand = "follow", permission = "teammt.mtprofiles.follow")
    public void handleFollow(Player sender, String target) {
        playerProfileManager.follow(sender, target);
    }

    /**
     * Unfollows a person's socials
     */
    @SubcommandInfo(subcommand = "unfollow", permission = "teammt.mtprofiles.unfollow")
    public void handleUnfollow(Player sender, String target) {
        playerProfileManager.unfollow(sender, target);
    }

    /**
     * Customizes message in socials GUI
     */
    @SubcommandInfo(subcommand = "status", permission = "teammt.mtprofiles.status")
    public void handleStatus(Player sender, String... statusList) {
        String status = "";
        for (String s : statusList)
            status += s + " ";
        if (status.length() > 30) {
            lib.getMessagesAPI().sendMessage("message-too-long", sender);
            return;
        }
        PlayerProfile profile = playerProfileManager.getPlayerProfile(sender.getUniqueId());
        long cooldown = lib.getConfigurationAPI().getConfig().getInt("status-cooldown");
        if (profile.getStatusLastChanged() + cooldown > Instant.now().getEpochSecond()) {
            long remainingTime = profile.getStatusLastChanged() + cooldown - Instant.now().getEpochSecond();
            Replaceable minutes;
            if (remainingTime / 60 != 1)
                minutes = new Replaceable("%minutes%", remainingTime / 60 + " minutes");
            else
                minutes = new Replaceable("%minutes%", remainingTime / 60 + " minute");
            Replaceable seconds;
            if (remainingTime % 60 != 1)
                seconds = new Replaceable("%seconds%", remainingTime % 60 + " seconds");
            else
                seconds = new Replaceable("%seconds%", remainingTime % 60 + " second");
            lib.getMessagesAPI().sendMessage("status-cooldown", sender, minutes, seconds);
            return;
        }
        profile.setStatus(status);
        profile.setStatusLastChanged(Instant.now().getEpochSecond());
        playerProfileManager.saveProfile(profile);
        lib.getMessagesAPI().sendMessage("status-added", sender);
    }

    /**
     * Removes customized message in socials GUI
     */
    @SubcommandInfo(subcommand = "removestatus", permission = "teammt.mtprofiles.removestatus")
    public void handleRemoveStatus(Player sender) {
        PlayerProfile profile = playerProfileManager.getPlayerProfile(sender.getUniqueId());
        if (profile.getStatus().equals("Welcome to my profile!")) {
            lib.getMessagesAPI().sendMessage("no-status", sender);
            return;
        }
        profile.removeStatus();
        playerProfileManager.saveProfile(profile);
        lib.getMessagesAPI().sendMessage("status-removed", sender);
    }
}
