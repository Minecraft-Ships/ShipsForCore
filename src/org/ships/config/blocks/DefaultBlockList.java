package org.ships.config.blocks;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.ships.config.parsers.ShipsParsers;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DefaultBlockList implements BlockList {

    protected ConfigurationFile file;
    protected Set<BlockInstruction> blocks = new HashSet<>();

    public DefaultBlockList(){
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/BlockList.yml");
        boolean created = file.exists();
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.YAML);
        if(!created){
            recreateFile();
        }
    }

    @Override
    public Set<BlockInstruction> getBlockList() {
        if(blocks.size() == 0){
            return reloadBlockList();
        }
        return this.blocks;
    }

    @Override
    public Set<BlockInstruction> reloadBlockList() {
        blocks.clear();
        CorePlugin.getPlatform().get(BlockType.class).stream().forEach(bt -> {
            Optional<BlockInstruction> opBlock = BlockList.getBlockInstruction(DefaultBlockList.this, bt);
            if(opBlock.isPresent()) {
                blocks.add(opBlock.get());
                return;
            }
            System.err.println("Failed to read block (" + bt.getId() + ") from BlockList configuration file");
            blocks.add(new BlockInstruction(bt).setCollideType(BlockInstruction.CollideType.DETECT_COLLIDE));
        });
        return this.blocks;
    }

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {
        ConfigurationFile file = getFile();
        Set<BlockType> completedBefore = new HashSet<>();
        addToConfig(BlockTypes.WALL_SIGN, BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.OAK_WOOD, BlockInstruction.CollideType.MATERIAL, completedBefore);
        CorePlugin.getPlatform().get(BlockType.class).stream().forEach(bt -> addToConfig(bt, BlockInstruction.CollideType.DETECT_COLLIDE, completedBefore));
        file.save();
    }

    private void addToConfig(BlockType type, BlockInstruction.CollideType collide, Set<BlockType> current){
        if(current.contains(type)){
            return;
        }
        String[] idSplit = type.getId().split(":");
        file.set(new ConfigurationNode("BlockList", idSplit[0], idSplit[1]), ShipsParsers.NODE_TO_BLOCK_INSTRUCTION, new BlockInstruction(type).setCollideType(collide));
        current.add(type);
    }
}
