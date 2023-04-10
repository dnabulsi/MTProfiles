package teammt.mtprofiles.data;

import org.bukkit.configuration.ConfigurationSection;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Platform {

    private String name;
    private String skull;
    private String color;

    public Platform(ConfigurationSection section) {
        this.name = section.getString("name");
        this.skull = section.getString("skull");
        this.color = section.getString("colorcode");
    }
}
