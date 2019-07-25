package org.ships.config.blocks;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.type.ConfigurationLoaderTypes;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.grouptype.BlockGroups;
import org.ships.config.parsers.ShipsParsers;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class DefaultBlockList implements BlockList {

    protected ConfigurationFile file;
    protected Set<BlockInstruction> blocks = new HashSet<>();

    public DefaultBlockList(){
        File file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "/Configuration/BlockList.temp");
        this.file = CorePlugin.createConfigurationFile(file, ConfigurationLoaderTypes.DEFAULT);
        if(!this.file.getFile().exists()){
            recreateFile();
            reloadBlockList();
        }
        this.file.reload();
    }

    @Override
    public Set<BlockInstruction> getBlockList() {
        if(blocks.isEmpty()){
            return reloadBlockList();
        }
        return this.blocks;
    }

    @Override
    public Set<BlockInstruction> reloadBlockList() {
        this.file.reload();
        blocks.clear();
        Collection<BlockType> mBlocks = CorePlugin.getPlatform().getBlockTypes();
        mBlocks.forEach(bt -> {
            Optional<BlockInstruction> opBlock = BlockList.getBlockInstruction(DefaultBlockList.this, bt);
            if(opBlock.isPresent()) {
                blocks.add(opBlock.get());
                return;
            }
            blocks.add(new BlockInstruction(bt).setCollideType(BlockInstruction.CollideType.DETECT_COLLIDE));
        });
        return this.blocks;
    }

    @Override
    public BlockList replaceBlockInstruction(BlockInstruction blockInstruction) {
        BlockInstruction bi = this.blocks.stream().filter(b -> b.getType().equals(blockInstruction.getType())).findAny().get();
        bi.setCollideType(blockInstruction.getCollideType());
        return this;
    }

    @Override
    public BlockList saveChanges() {
        this.blocks.stream().forEach(b -> {
            String[] idSplit = b.getType().getId().split(":");
            file.set(new ConfigurationNode("BlockList", idSplit[0], idSplit[1]), ShipsParsers.NODE_TO_BLOCK_INSTRUCTION, b);
        });
        file.save();
        return this;
    }

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {
        ConfigurationFile file = getFile();
        Set<BlockType> completedBefore = new HashSet<>();
        BlockTypes.BLACK_WOOL.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.OAK_PLANKS.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.OAK_LOG.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.BLACK_CARPET.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.ACACIA_LOG.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.BLACK_BANNER.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.OAK_SIGN.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.OAK_WALL_SIGN.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        //BlockTypes.ACACIA_LEAVES.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.PURPUR_BLOCK.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        //BlockTypes.PUMPKIN.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.ANVIL.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.BLACK_GLAZED_TERRACOTTA.get().getLike().forEach(w -> addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.BUTTON.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.BED.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.CONCRETE.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.CONCRETE_POWDER.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.SHULKER_BOX.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.TERRACOTTA.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.PRESSURE_PLATE.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.FENCE.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.FENCE_GATE.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.TORCH.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.STAINED_GLASS.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.STAINED_GLASS_PANE.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.DOOR.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.TRAP_DOOR.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.SLAB.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.STAIRS.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.SAPLING.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.POTTED_SAPLING.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups.PISTON.getGrouped()).forEach(t -> addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));

        addToConfig(BlockTypes.JUKEBOX.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.LEVER.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.LADDER.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.CRAFTING_TABLE.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.BRICKS.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.TNT.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.BOOKSHELF.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.GOLD_BLOCK.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.IRON_BLOCK.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.DIAMOND_BLOCK.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.NOTE_BLOCK.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.DISPENSER.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.LAPIS_BLOCK.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.GLASS.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.FURNACE.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.CHEST.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.ENDER_CHEST.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.TRAPPED_CHEST.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.GLASS.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.GLASS_PANE.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.FIRE.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.NETHERRACK.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.GRASS_BLOCK.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.ENCHANTING_TABLE.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.REDSTONE_LAMP.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.EMERALD_BLOCK.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.BEACON.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.TRAPPED_CHEST.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.DAYLIGHT_DETECTOR.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.TRAPPED_CHEST.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.HOPPER.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.DROPPER.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.HAY_BLOCK.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.OBSERVER.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.REPEATER.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.COMPARATOR.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);
        addToConfig(BlockTypes.REDSTONE_WIRE.get(), BlockInstruction.CollideType.MATERIAL, completedBefore);

        CorePlugin.getPlatform().getBlockTypes().forEach(bt -> addToConfig(bt, BlockInstruction.CollideType.DETECT_COLLIDE, completedBefore));
        file.save();
    }

    private void addToConfig(BlockType type, BlockInstruction.CollideType collide, Set<BlockType> current){
        if(current.stream().anyMatch(c -> c.equals(type))){
            return;
        }
        String[] idSplit = type.getId().split(":");
        file.set(new ConfigurationNode("BlockList", idSplit[0], idSplit[1]), ShipsParsers.NODE_TO_BLOCK_INSTRUCTION, new BlockInstruction(type).setCollideType(collide));
        current.add(type);
    }
}
