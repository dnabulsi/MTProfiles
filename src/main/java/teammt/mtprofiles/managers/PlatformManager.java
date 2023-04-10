package teammt.mtprofiles.managers;

import java.util.ArrayList;
import java.util.List;

import masecla.mlib.classes.Registerable;
import masecla.mlib.main.MLib;
import teammt.mtprofiles.data.Platform;

public class PlatformManager extends Registerable {

    public PlatformManager(MLib lib) {
        super(lib);
    }

    public List<String> getPlatformNames() {
        return new ArrayList<>(
                lib.getConfigurationAPI().getConfig().getConfigurationSection("platforms").getKeys(false));
    }

    public List<Platform> getPlatforms() {
        List<Platform> list = new ArrayList<>();
        for (String cr : getPlatformNames()) {
            list.add(new Platform(lib.getConfigurationAPI().getConfig().getConfigurationSection("platforms." + cr)));
        }
        return list;
    }

    public Platform getPlatform(String platform) {
        for (Platform cr : getPlatforms()) {
            if (cr.getName().equalsIgnoreCase(platform))
                return cr;
        }
        return null;
    }
}