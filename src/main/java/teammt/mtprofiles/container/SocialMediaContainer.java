package teammt.mtprofiles.container;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import masecla.mlib.classes.Replaceable;
import masecla.mlib.classes.builders.InventoryBuilder;
import masecla.mlib.classes.builders.ItemBuilder;
import masecla.mlib.containers.generic.ImmutableContainer;
import masecla.mlib.main.MLib;
import teammt.mtprofiles.data.Platform;
import teammt.mtprofiles.data.PlayerProfile;
import teammt.mtprofiles.managers.PlatformManager;
import teammt.mtprofiles.managers.PlayerProfileManager;

public class SocialMediaContainer extends ImmutableContainer {

    private PlatformManager platformManager;
    private PlayerProfileManager playerProfileManager;

    public SocialMediaContainer(MLib lib, PlatformManager platformManager, PlayerProfileManager playerProfileManager) {
        super(lib);
        this.platformManager = platformManager;
        this.playerProfileManager = playerProfileManager;
    }

    @Override
    public void onTopClick(InventoryClickEvent event) {
        if (event.getSlot() == 31) {
            event.getWhoClicked().closeInventory();
            return;
        }
        if (event.getSlot() == 35) {
            Player player = (Player) event.getWhoClicked();
            playerProfileManager.setLookingCurrentlyFollowing(player.getUniqueId(),
                    playerProfileManager.getLookingSocialMedia(player.getUniqueId()));
            lib.getContainerAPI().openFor((Player) event.getWhoClicked(), CurrentlyFollowingContainer.class);
            return;
        }
    }

    @Override
    public int getSize(Player player) {
        return 36;
    }

    @Override
    public int getUpdatingInterval() {
        return 10;
    }

    @Override
    public boolean requiresUpdating() {
        return true;
    }

    @Override
    public Inventory getInventory(Player player) {
        UUID target = playerProfileManager.getLookingSocialMedia(player.getUniqueId());
        PlayerProfile targetProfile = playerProfileManager.getPlayerProfile(target);
        InventoryBuilder inventory = new InventoryBuilder().size(getSize(player)).messages()
                .title("platforms-container-title").replaceable("%target%", targetProfile.getName())
                .border(getSocialMediaContainerBorder())
                .setItem(4, getPlayerSkull(targetProfile))
                .setItem(35, getFollowingContainer(targetProfile))
                .setItem(31, getSocialMediaContainerClose())
                .setItem(0, getTimes(targetProfile));
        inventory = getSocialMediaContainerPlatforms(inventory, targetProfile);
        return inventory.build(lib, player);
    }

    private InventoryBuilder getSocialMediaContainerPlatforms(InventoryBuilder result, PlayerProfile targetProfile) {
        int[] availableSlots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25 };
        for (int i = 0; i < platformManager.getPlatforms().size(); i++) {
            Platform platform = platformManager.getPlatforms().get(i);
            ItemBuilder platformItem = new ItemBuilder().skull(platform.getSkull()).mnl("platforms-container-object")
                    .replaceable("%color%", platform.getColor()).replaceable("%platform%", platform.getName());
            if (targetProfile.getPlatformTags().containsKey(platform))
                platformItem.replaceable("%tag%", targetProfile.getPlatformTags().get(platform));
            else
                platformItem.replaceable("%tag%", "N/A");
            result.setItem(availableSlots[i],
                    platformItem);
        }
        return result;
    }

    private ItemStack getSocialMediaContainerBorder() {
        Material pane = null;
        if (lib.getCompatibilityApi().getServerVersion().getMajor() <= 12)
            pane = Material.matchMaterial("STAINED_GLASS_PANE");
        else
            pane = Material.matchMaterial("BLACK_STAINED_GLASS_PANE");
        ItemBuilder paneItem = new ItemBuilder(pane);
        if (lib.getCompatibilityApi().getServerVersion().getMajor() <= 12)
            paneItem = paneItem.data((byte) 15);
        return paneItem.name(" ").build(lib);
    }

    private ItemStack getSocialMediaContainerClose() {
        return new ItemBuilder(Material.BARRIER)
                .mnl("platforms-container-close")
                .build(lib);
    }

    private ItemStack getFollowingContainer(PlayerProfile target) {
        return new ItemBuilder(Material.FILLED_MAP)
                .mnl("platforms-container-following").replaceable("%target%", target.getName())
                .build(lib);
    }

    private ItemStack getPlayerSkull(PlayerProfile targetProfile) {
        ItemStack skull = targetProfile.getSkull();
        ItemMeta meta = skull.getItemMeta();
        String name = lib.getMessagesAPI().getPluginMessage("platforms-container-skull-name",
                new Replaceable("%target%", targetProfile.getName()));
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>(lib.getMessagesAPI().getPluginListMessage("platforms-container-skull-lore",
                new Replaceable("%status%",
                        playerProfileManager.getPlayerProfile(targetProfile.getUuid()).getStatus())));
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack getTimes(PlayerProfile profile) {
        ItemBuilder item = new ItemBuilder(Material.CLOCK).mnl("platforms-container-times").replaceable("%target%",
                profile.getName());
        DateFormat datetime = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss zzz");
        datetime.setTimeZone(TimeZone.getTimeZone(lib.getConfigurationAPI().getConfig().getString("timezone")));
        if (profile.getFirstLoggedIn() == 0)
            item.replaceable("%first-in%", "N/A");
        else
            item.replaceable("%first-in%", datetime.format(profile.getFirstLoggedIn() * 1000));
        if (profile.getLastLoggedIn() == 0)
            item.replaceable("%last-in%", "N/A");
        else
            item.replaceable("%last-in%", datetime.format(profile.getLastLoggedIn() * 1000));
        if (profile.getLastLoggedOut() == 0)
            item.replaceable("%last-out%", "N/A");
        else
            item.replaceable("%last-out%", datetime.format(profile.getLastLoggedOut() * 1000));
        return item.build(lib);
    }

}
