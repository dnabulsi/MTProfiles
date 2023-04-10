package teammt.mtprofiles.container;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.Setter;
import masecla.mlib.classes.Replaceable;
import masecla.mlib.containers.instances.SquaredPagedContainer;
import masecla.mlib.main.MLib;
import teammt.mtprofiles.data.PlayerProfile;
import teammt.mtprofiles.managers.PlayerProfileManager;

public class CurrentlyFollowingContainer extends SquaredPagedContainer {

    private PlayerProfileManager playerProfileManager;
    @Setter
    private SocialMediaContainer socialMediaContainer;

    public CurrentlyFollowingContainer(MLib lib, PlayerProfileManager playerProfileManager) {
        super(lib);
        this.playerProfileManager = playerProfileManager;
    }

    @Override
    public void usableClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;
        String tag = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);
        if (tag == null)
            return;
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            if (op.getName().equals(tag)) {
                playerProfileManager.setLookingSocialMedia(((Player) event.getWhoClicked()).getUniqueId(),
                        op.getUniqueId());
                lib.getContainerAPI().openFor((Player) event.getWhoClicked(), SocialMediaContainer.class);
                return;
            }
        }
    }

    @Override
    public List<ItemStack> getOrderableItems(Player player) {
        List<ItemStack> result = new ArrayList<>();
        UUID targetUuid = playerProfileManager.getLookingCurrentlyFollowing(player.getUniqueId());
        PlayerProfile targetProfile = playerProfileManager.getPlayerProfile(targetUuid);
        for (UUID uuid : targetProfile.getFollowing()) {
            PlayerProfile currentProfile = playerProfileManager.getPlayerProfile(uuid);
            ItemStack skull = playerProfileManager.getPlayerProfile(uuid).getSkull();
            ItemMeta skullMeta = skull.getItemMeta();
            skullMeta.setDisplayName(lib.getMessagesAPI().getPluginMessage("following-container-skull-name", player,
                    new Replaceable("%target%", currentProfile.getName())));
            skullMeta.setLore(lib.getMessagesAPI().getPluginListMessage("following-container-skull-lore", player,
                    new Replaceable("%target%", currentProfile.getName())));
            skull.setItemMeta(skullMeta);
            result.add(skull);
        }
        return result;
    }

    @Override
    public int getSize(Player player) {
        return 27;
    }

    @Override
    public int getUpdatingInterval() {
        return 10;
    }

    @Override
    public String getTitle(Player player) {
        PlayerProfile targetProfile = playerProfileManager
                .getPlayerProfile(playerProfileManager.getLookingCurrentlyFollowing(player.getUniqueId()));
        return lib.getMessagesAPI().getPluginMessage("following-container-title", player,
                new Replaceable("%target%", targetProfile.getName()));
    }

}
