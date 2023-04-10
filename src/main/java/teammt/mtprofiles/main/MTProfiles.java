package teammt.mtprofiles.main;

import org.bukkit.plugin.java.JavaPlugin;

import masecla.mlib.main.MLib;
import teammt.mtprofiles.commands.MTProfilesCommand;
import teammt.mtprofiles.container.CurrentlyFollowingContainer;
import teammt.mtprofiles.container.SocialMediaContainer;
import teammt.mtprofiles.managers.PlatformManager;
import teammt.mtprofiles.managers.PlayerProfileManager;

public final class MTProfiles extends JavaPlugin {

    private MLib lib;
    private PlayerProfileManager playerProfileManager;
    private PlatformManager platformManager;
    private SocialMediaContainer socialMediaContainer;
    private CurrentlyFollowingContainer currentlyFollowingContainer;

    @Override
    public void onEnable() {

        this.lib = new MLib(this);
        this.lib.getConfigurationAPI().requireAll();
        this.lib.getMessagesAPI().disableAntispam();

        // Managers
        this.platformManager = new PlatformManager(lib);
        this.platformManager.register();
        this.playerProfileManager = new PlayerProfileManager(lib, platformManager);
        this.playerProfileManager.register();
        this.currentlyFollowingContainer = new CurrentlyFollowingContainer(lib, playerProfileManager);
        this.currentlyFollowingContainer.register();
        this.socialMediaContainer = new SocialMediaContainer(lib, platformManager, playerProfileManager);
        this.socialMediaContainer.register();
        this.currentlyFollowingContainer.setSocialMediaContainer(socialMediaContainer);

        // Commands
        new MTProfilesCommand(lib, playerProfileManager, platformManager, socialMediaContainer).register();
    }

}
