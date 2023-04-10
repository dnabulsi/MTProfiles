package teammt.mtprofiles.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import lombok.Data;
import lombok.NoArgsConstructor;
import masecla.mlib.main.MLib;

@Data
@NoArgsConstructor
public class PlayerProfile {

    private UUID uuid;
    private String name;
    private ItemStack skull;
    private String status = "Welcome to my profile!";
    private long statusLastChanged = 0;
    private long firstLoggedIn = Instant.now().getEpochSecond();
    private long lastLoggedIn = 0;
    private long lastLoggedOut = 0;
    private List<UUID> following = new ArrayList<>();
    private Map<Platform, String> platformTags = new HashMap<>();

    public PlayerProfile(Player player, MLib lib) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.skull = getSkull(player.getUniqueId(), lib);
    }

    public void addFollowing(UUID uuid) {
        following.add(uuid);
    }

    public void removeFollowing(UUID uuid) {
        following.remove(uuid);
    }

    public void removeStatus() {
        this.status = "Welcome to my profile!";
    }

    @SuppressWarnings("deprecation")
    private ItemStack getSkull(UUID uuid, MLib lib) {
        Player player = Bukkit.getPlayer(uuid);
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (lib.getCompatibilityApi().getServerVersion().getMajor() >= 13)
            skullMeta.setOwningPlayer(player);
        else
            skullMeta.setOwner(player.getName());
        playerHead.setItemMeta(skullMeta);
        return playerHead;
    }

}