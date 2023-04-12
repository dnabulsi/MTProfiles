package teammt.mtprofiles.managers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import masecla.mlib.classes.Registerable;
import masecla.mlib.classes.Replaceable;
import masecla.mlib.main.MLib;
import teammt.mtprofiles.data.Platform;
import teammt.mtprofiles.data.PlayerProfile;

public class PlayerProfileManager extends Registerable {
    private PlatformManager platformManager;
    private Map<UUID, UUID> lookingCurrentlyFollowingContainer = new HashMap<>();
    private Map<UUID, UUID> lookingSocialMediaContainer = new HashMap<>();

    public PlayerProfileManager(MLib lib, PlatformManager platformManager) {
        super(lib);
        this.platformManager = platformManager;
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PlayerProfile profile = getPlayerProfile(e.getPlayer().getUniqueId());
        if (profile == null) {
            profile = new PlayerProfile(e.getPlayer(), lib);
        }
        profile.setLastLoggedIn(Instant.now().getEpochSecond());
        saveProfile(profile);
        List<String> list = (ArrayList<String>) lib.getConfigurationAPI().getConfig("config").get("force-to-follow");
        for (String cr : list) {
            OfflinePlayer offlinePlayer = null;
            for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                if (op.getName().equalsIgnoreCase(cr)) {
                    offlinePlayer = op;
                    break;
                }
            }
            if (offlinePlayer == null) {
                System.out.println("Player '" + cr + "' does not exist! Check config.yml.");
                return;
            }
            UUID targetUuid = offlinePlayer.getUniqueId();
            if (getPlayerProfile(targetUuid) == null) {
                System.out.println("Cannot find player social profile for '" + cr + "'! Check config.yml.");
                return;
            } else if (!targetUuid.equals(e.getPlayer().getUniqueId()) && (!profile.getFollowing().contains(targetUuid)))
                profile.addFollowing(targetUuid);
        }
        saveProfile(profile);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerProfile profile = getPlayerProfile(e.getPlayer().getUniqueId());
        profile.setLastLoggedOut(Instant.now().getEpochSecond());
        saveProfile(profile);
    }

    public PlayerProfile getPlayerProfile(UUID uuid) {
        return (PlayerProfile) lib.getConfigurationAPI().getConfig("data").get("profile." + uuid.toString());
    }

    public void saveProfile(PlayerProfile profile) {
        lib.getConfigurationAPI().getConfig("data").set("profile." + profile.getUuid(), profile);
    }

    public void link(Player player, String platform, String tag) {
        List<String> platformsList = platformManager.getPlatformNames();
        if (platformsList.contains(platform)) {
            PlayerProfile profile = getPlayerProfile(player.getUniqueId());
            Platform platformData = platformManager.getPlatform(platform);
            profile.getPlatformTags().put(platformData, tag);
            saveProfile(profile);
            lib.getMessagesAPI().sendMessage("platform-added", player, new Replaceable("%tag%", tag),
                    new Replaceable("%platform%", platformData.getName()));
        } else
            lib.getMessagesAPI().sendMessage("platform-not-found", player, new Replaceable("%platform%", platform));
    }

    public void unlink(Player player, String platform) {
        List<String> platformsList = platformManager.getPlatformNames();
        if (platformsList.contains(platform)) {
            PlayerProfile profile = getPlayerProfile(player.getUniqueId());
            Platform platformData = platformManager.getPlatform(platform);
            profile.getPlatformTags().remove(platformData);
            saveProfile(profile);
            lib.getMessagesAPI().sendMessage("platform-removed", player,
                    new Replaceable("%platform%", platformData.getName()));
        } else
            lib.getMessagesAPI().sendMessage("platform-not-found", player);
    }

    public void follow(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            lib.getMessagesAPI().sendMessage("player-not-found", sender, new Replaceable("%player%", targetName));
            return;
        } else if (target.equals(sender)) {
            lib.getMessagesAPI().sendMessage("cannot-follow-self", sender);
            return;
        }
        PlayerProfile senderProfile = getPlayerProfile(sender.getUniqueId());
        if (senderProfile.getFollowing().contains(target.getUniqueId())) {
            lib.getMessagesAPI().sendMessage("already-following", sender,
                    new Replaceable("%target%", target.getName()));
            return;
        }
        senderProfile.addFollowing(target.getUniqueId());
        saveProfile(senderProfile);
        lib.getMessagesAPI().sendMessage("now-following", sender, new Replaceable("%target%", target.getName()));
    }

    public void unfollow(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            lib.getMessagesAPI().sendMessage("player-not-found", sender, new Replaceable("%player%", targetName));
            return;
        }
        PlayerProfile senderProfile = getPlayerProfile(sender.getUniqueId());
        if (!senderProfile.getFollowing().contains(target.getUniqueId())) {
            lib.getMessagesAPI().sendMessage("not-following", sender, new Replaceable("%target%", target.getName()));
            return;
        }
        senderProfile.removeFollowing(target.getUniqueId());
        saveProfile(senderProfile);
        lib.getMessagesAPI().sendMessage("no-longer-following", sender, new Replaceable("%target%", target.getName()));
    }

    public void setLookingCurrentlyFollowing(UUID player, UUID target) {
        lookingCurrentlyFollowingContainer.put(player, target);
    }

    public void setLookingSocialMedia(UUID player, UUID target) {
        lookingSocialMediaContainer.put(player, target);
    }

    public UUID getLookingCurrentlyFollowing(UUID uuid) {
        return lookingCurrentlyFollowingContainer.get(uuid);
    }

    public UUID getLookingSocialMedia(UUID uuid) {
        return lookingSocialMediaContainer.get(uuid);
    }

    @EventHandler
    public void onInventoryClose(PlayerQuitEvent event) {
        lookingCurrentlyFollowingContainer.remove(event.getPlayer().getUniqueId());
        lookingSocialMediaContainer.remove(event.getPlayer().getUniqueId());
    }

}
