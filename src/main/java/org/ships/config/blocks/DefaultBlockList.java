package org.ships.config.blocks;

import org.core.TranslateCore;
import org.core.config.ConfigurationFormat;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.blocktypes.post.BlockTypes1V13;
import org.core.world.position.block.grouptype.versions.BlockGroups1V13;
import org.core.world.position.block.grouptype.versions.CommonBlockGroups;
import org.ships.config.parsers.ShipsParsers;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Stream;

public class DefaultBlockList implements BlockList {

    protected final ConfigurationStream.ConfigurationFile file;
    protected final LinkedTransferQueue<BlockInstruction> blocks = new LinkedTransferQueue<>();

    public DefaultBlockList() {
        ConfigurationFormat format = TranslateCore.getPlatform().getConfigFormat();
        File file = new File(ShipsPlugin.getPlugin().getConfigFolder(),
                "Configuration/BlockList." + format.getFileType()[0]);
        this.file = TranslateCore.createConfigurationFile(file, format);
        if (!this.file.getFile().exists()) {
            this.recreateFile();
            this.reloadBlockList();
        }
        this.file.reload();
    }

    @Override
    public Collection<BlockInstruction> getBlockList() {
        if (this.blocks.isEmpty()) {
            synchronized (this) {
                if (this.blocks.isEmpty()) {
                    return this.reloadBlockList();
                }
                return Collections.unmodifiableCollection(this.blocks);
            }
        }
        return Collections.unmodifiableCollection(this.blocks);
    }

    @Override
    public synchronized Collection<BlockInstruction> reloadBlockList() {
        this.file.reload();
        this.blocks.clear();
        Collection<BlockType> mBlocks = TranslateCore.getPlatform().getBlockTypes();
        mBlocks.forEach(bt -> {
            Optional<BlockInstruction> opBlock = BlockList.getBlockInstruction(DefaultBlockList.this, bt);
            if (opBlock.isPresent()) {
                this.blocks.add(opBlock.get());
                return;
            }
            this.blocks.add(new BlockInstruction(bt).setCollideType(BlockInstruction.CollideType.DETECT_COLLIDE));
        });
        return Collections.unmodifiableCollection(this.blocks);
    }

    @Override
    public BlockList replaceBlockInstruction(BlockInstruction blockInstruction) {
        BlockInstruction bi = this.blocks.stream().filter(b -> b.getType().equals(blockInstruction.getType())).findAny().orElseThrow(() -> new IllegalArgumentException("Could not find Block instruction for " + blockInstruction.getType().getName()));
        bi.setCollideType(blockInstruction.getCollideType());
        bi.setBlockLimit(blockInstruction.getBlockLimit());
        return this;
    }

    @Override
    public synchronized BlockList saveChanges() {
        this.blocks.forEach(b -> {
            String[] idSplit = b.getType().getId().split(":");
            this.file.set(new ConfigurationNode("BlockList", idSplit[0], idSplit[1]), ShipsParsers.NODE_TO_BLOCK_INSTRUCTION, b);
        });
        this.file.save();
        return this;
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public synchronized void recreateFile() {
        ConfigurationStream.ConfigurationFile file = this.getFile();
        Collection<BlockType> completedBefore = new HashSet<>();
        BlockTypes.OAK_SIGN.getLike().forEach(w -> this.addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.OAK_WALL_SIGN.getLike().forEach(w -> this.addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.PURPUR_BLOCK.getLike().forEach(w -> this.addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.ANVIL.getLike().forEach(w -> this.addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        BlockTypes.BLACK_GLAZED_TERRACOTTA.getLike().forEach(w -> this.addToConfig(w, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(CommonBlockGroups.SHULKER_BOX.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(CommonBlockGroups.FENCE.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(CommonBlockGroups.FENCE_GATE.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(CommonBlockGroups.DOOR.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(CommonBlockGroups.PISTON.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));

        this.addToConfig(BlockTypes.JUKEBOX, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.LEVER, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.LADDER, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.CRAFTING_TABLE, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.BRICKS, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.TNT, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.BOOKSHELF, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.GOLD_BLOCK, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.IRON_BLOCK, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.DIAMOND_BLOCK, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.NOTE_BLOCK, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.DISPENSER, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.LAPIS_BLOCK, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.GLASS, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.FURNACE, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.CHEST, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.ENDER_CHEST, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.TRAPPED_CHEST, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.GLASS, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.GLASS_PANE, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.FIRE, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.NETHERRACK, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.ENCHANTING_TABLE, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.REDSTONE_LAMP, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.EMERALD_BLOCK, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.BEACON, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.TRAPPED_CHEST, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.DAYLIGHT_DETECTOR, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.TRAPPED_CHEST, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.HOPPER, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.DROPPER, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.HAY_BLOCK, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.OBSERVER, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.REDSTONE_WIRE, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.CAULDRON, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.CAVE_AIR, BlockInstruction.CollideType.IGNORE, completedBefore);
        this.addToConfig(BlockTypes.TALL_GRASS, BlockInstruction.CollideType.IGNORE, completedBefore);

        this.addToConfig(BlockTypes.REDSTONE_WIRE, BlockInstruction.CollideType.IGNORE, completedBefore);

        Stream.of(BlockGroups1V13.LOG.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.WOOD_PLANKS.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.BANNER.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.CARPET.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.WOOL.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.BUTTON.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.BED.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.CONCRETE.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.CONCRETE_POWDER.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.SLAB.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.STAIRS.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.POTTED_SAPLING.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.TORCH.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.STAINED_GLASS.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.STAINED_GLASS_PANE.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.TERRACOTTA.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.PRESSURE_PLATE.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));
        Stream.of(BlockGroups1V13.TRAP_DOOR.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.MATERIAL, completedBefore));


        Stream.of(BlockGroups1V13.SAPLING.getGrouped()).forEach(t -> this.addToConfig(t, BlockInstruction.CollideType.IGNORE, completedBefore));
        this.addToConfig(BlockTypes1V13.DANDELION, BlockInstruction.CollideType.IGNORE, completedBefore);
        this.addToConfig(BlockTypes1V13.KELP, BlockInstruction.CollideType.IGNORE, completedBefore);
        this.addToConfig(BlockTypes1V13.REPEATER, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes1V13.COMPARATOR, BlockInstruction.CollideType.MATERIAL, completedBefore);
        this.addToConfig(BlockTypes.CAVE_AIR, BlockInstruction.CollideType.IGNORE, completedBefore);
        this.addToConfig(BlockTypes.TALL_GRASS, BlockInstruction.CollideType.IGNORE, completedBefore);
        this.addToConfig(BlockTypes.TALL_SEAGRASS, BlockInstruction.CollideType.IGNORE, completedBefore);
        this.addToConfig(BlockTypes.SEAGRASS, BlockInstruction.CollideType.IGNORE, completedBefore);

        TranslateCore.getPlatform().getBlockTypes().forEach(bt -> this.addToConfig(bt, BlockInstruction.CollideType.DETECT_COLLIDE, completedBefore));
        file.save();
    }

    private void addToConfig(BlockType type, BlockInstruction.CollideType collide, Collection<? super BlockType> current) {
        if (current.stream().anyMatch(c -> c.equals(type))) {
            return;
        }
        String[] idSplit = type.getId().split(":");
        this.file.set(new ConfigurationNode("BlockList", idSplit[0], idSplit[1]), ShipsParsers.NODE_TO_BLOCK_INSTRUCTION, new BlockInstruction(type).setCollideType(collide));
        current.add(type);
    }
}
