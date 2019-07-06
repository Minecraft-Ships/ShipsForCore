package org.ships.config.configuration;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.ships.config.Config;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.Optional;

public class LegacyShipsConfig implements Config {

    protected ConfigurationFile configuration;

    public final ConfigurationNode LEGACY_STRUCTURE_LIMITS_AIR_CHECK_GAP = new ConfigurationNode("Structure", "StructureLimits", "airCheckGap");
    public final ConfigurationNode LEGACY_STRUCTURE_LIMITS_TRACK_LIMIT = new ConfigurationNode("Structure", "StructureLimits", "trackLimit");
    public final ConfigurationNode LEGACY_SIGN_FORCE_USER_NAME_ON_SIGN = new ConfigurationNode("Structure", "Signs", "ForceUsernameOnLicenceSign");
    public final ConfigurationNode LEGACY_SIGN_EOT_REPEAT = new ConfigurationNode("Structure", "Signs", "EOT", "repeat");
    public final ConfigurationNode LEGACY_SIGN_EOT_ENABLED = new ConfigurationNode("Structure", "Signs", "EOT", "enabled");
    public final ConfigurationNode LEGACY_INVENTORY_KEEP_INVENTORY_OPEN = new ConfigurationNode("Structure", "Signs", "keepInventorysOpen");

    public LegacyShipsConfig() {
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "Configuration/Config.yml");
        this.configuration = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.YAML);
    }

    @Override
    public ConfigurationFile getFile() {
        return this.configuration;
    }

    @Override
    public void recreateFile() {

    }

    public boolean isLegacy(){
        return getFile().parseInt(this.LEGACY_STRUCTURE_LIMITS_TRACK_LIMIT).isPresent();
    }

    public ShipsConfig convertToNew(){
        Optional<Boolean> opForceUsername = getFile().parseBoolean(LEGACY_SIGN_FORCE_USER_NAME_ON_SIGN);
        Optional<Integer> opEOTRepeat = getFile().parseInt(LEGACY_SIGN_EOT_REPEAT);
        Optional<Boolean> opEOTEnabled = getFile().parseBoolean(LEGACY_SIGN_EOT_ENABLED);
        Optional<Integer> opTrack = getFile().parseInt(LEGACY_STRUCTURE_LIMITS_TRACK_LIMIT);
        this.configuration.getFile().delete();
        ShipsConfig config = new ShipsConfig();
        opForceUsername.ifPresent(b -> config.file.set(config.LICENCE_SIGN_TEXT_4TH, b ? "%PlayerN%" : ""));
        opEOTRepeat.ifPresent(t -> {
            config.file.set(config.EOT_DELAY, t);
            config.file.set(config.EOT_DELAY_UNIT, "");
        });
        opEOTEnabled.ifPresent(b -> {
            if(b){
                config.file.set(config.EOT_SPEED, 0);
            }
        });
        opTrack.ifPresent(i -> config.file.set(config.ADVANCED_TRACK_LIMIT, i));
        return config;
    }
}
