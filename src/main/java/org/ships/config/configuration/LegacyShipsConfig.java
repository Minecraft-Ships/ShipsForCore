package org.ships.config.configuration;

import org.core.TranslateCore;
import org.core.config.ConfigurationFormat;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.schedule.unit.TimeUnit;
import org.ships.config.Config;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.Optional;

public class LegacyShipsConfig implements Config {

    public final ConfigurationNode.KnownParser.SingleKnown<Integer> LEGACY_STRUCTURE_LIMITS_AIR_CHECK_GAP =
            new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Structure", "StructureLimits", "airCheckGap");
    public final ConfigurationNode.KnownParser.SingleKnown<Integer> LEGACY_STRUCTURE_LIMITS_TRACK_LIMIT =
            new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Structure", "StructureLimits", "trackLimit");
    public final ConfigurationNode.KnownParser.SingleKnown<Boolean> LEGACY_SIGN_FORCE_USER_NAME_ON_SIGN =
            new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_BOOLEAN, "Structure", "Signs", "ForceUsernameOnLicenceSign");
    public final ConfigurationNode.KnownParser.SingleKnown<Integer> LEGACY_SIGN_EOT_REPEAT =
            new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_INTEGER, "Structure", "Signs", "EOT", "repeat");
    public final ConfigurationNode.KnownParser.SingleKnown<Boolean> LEGACY_SIGN_EOT_ENABLED =
            new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_BOOLEAN, "Structure", "Signs", "EOT", "enabled");
    public final ConfigurationNode.KnownParser.SingleKnown<Boolean> LEGACY_INVENTORY_KEEP_INVENTORY_OPEN =
            new ConfigurationNode.KnownParser.SingleKnown<>(
            Parser.STRING_TO_BOOLEAN, "Structure", "Signs", "keepInventorysOpen");
    protected final ConfigurationStream.ConfigurationFile configuration;

    public LegacyShipsConfig() {
        File file = new File(ShipsPlugin.getPlugin().getConfigFolder(), "Configuration/Config.yml");
        this.configuration = TranslateCore.getConfigManager().read(file, ConfigurationFormat.FORMAT_YAML);
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.configuration;
    }

    @Override
    public void recreateFile() {

    }

    public boolean isLegacy() {
        return this.getFile().getInteger(this.LEGACY_STRUCTURE_LIMITS_TRACK_LIMIT).isPresent();
    }

    public ShipsConfig convertToNew() {
        Optional<Boolean> opForceUsername = this.getFile().getBoolean(this.LEGACY_SIGN_FORCE_USER_NAME_ON_SIGN);
        Optional<Integer> opEOTRepeat = this.getFile().getInteger(this.LEGACY_SIGN_EOT_REPEAT);
        Optional<Boolean> opEOTEnabled = this.getFile().getBoolean(this.LEGACY_SIGN_EOT_ENABLED);
        Optional<Integer> opTrack = this.getFile().getInteger(this.LEGACY_STRUCTURE_LIMITS_TRACK_LIMIT);
        this.configuration.getFile().delete();
        ShipsConfig config = new ShipsConfig();
        opForceUsername.ifPresent(
                b -> config.file.set(config.LICENCE_SIGN_TEXT_4TH.getNode(), Parser.STRING_TO_STRING_PARSER,
                        b ? "%PlayerN%" : ""));
        opEOTRepeat.ifPresent(t -> {
            config.file.set(config.EOT_DELAY.getNode(), t);
            config.file.set(config.EOT_DELAY_UNIT.getNode(), TimeUnit.MINECRAFT_TICKS);
        });
        opEOTEnabled.ifPresent(b -> {
            if (b) {
                config.file.set(config.EOT_SPEED.getNode(), 0);
            }
        });
        opTrack.ifPresent(i -> config.file.set(config.ADVANCED_TRACK_LIMIT.getNode(), i));
        return config;
    }
}
