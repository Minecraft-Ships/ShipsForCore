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
import org.ships.config.blocks.instruction.BlockInstruction;
import org.ships.config.blocks.instruction.CollideType;
import org.ships.config.blocks.instruction.ModifiableBlockInstruction;
import org.ships.config.blocks.instruction.MoveIntoBlockInstruction;
import org.ships.config.parsers.ShipsParsers;
import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultBlockList implements BlockList {

    protected final ConfigurationStream.ConfigurationFile file;
    protected final LinkedTransferQueue<ModifiableBlockInstruction> blocks = new LinkedTransferQueue<>();

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
        Collection<BlockInstruction> blockInstructions = new LinkedList<>(this.blocks);
        blockInstructions.addAll(this.blocks);
        Set<MoveIntoBlockInstruction> moveIn = ShipsPlugin
                .getPlugin()
                .getAllShipTypes()
                .parallelStream()
                .flatMap(type -> Stream.of(type.getIgnoredTypes()))
                .map(MoveIntoBlockInstruction::new)
                .collect(Collectors.toSet());
        blockInstructions.addAll(moveIn);
        return Collections.unmodifiableCollection(blockInstructions);
    }

    @Override
    public synchronized Collection<BlockInstruction> reloadBlockList() {
        this.file.reload();
        this.blocks.clear();

        Set<BlockType> moveInTypes = ShipsPlugin
                .getPlugin()
                .getAllShipTypes()
                .parallelStream()
                .flatMap(type -> Stream.of(type.getIgnoredTypes()))
                .collect(Collectors.toSet());


        Collection<BlockType> mBlocks = TranslateCore.getPlatform().getBlockTypes();
        mBlocks.forEach(bt -> {
            Optional<ModifiableBlockInstruction> opBlock = BlockList.getBlockInstruction(DefaultBlockList.this, bt);
            if (opBlock.isPresent()) {
                this.blocks.add(opBlock.get());
                return;
            }
            if (moveInTypes.contains(bt)) {
                return;
            }
            this.blocks.add(new ModifiableBlockInstruction(bt).setCollide(CollideType.DETECT_COLLIDE));
        });
        return Collections.unmodifiableCollection(this.blocks);
    }

    @Override
    public BlockList replaceBlockInstruction(BlockInstruction blockInstruction) {
        BlockInstruction bi = this.blocks
                .stream()
                .filter(b -> b.getType().equals(blockInstruction.getType()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find Block instruction for " + blockInstruction.getType().getName()));
        if (bi instanceof ModifiableBlockInstruction mbi) {
            mbi.setCollide(blockInstruction.getCollide());
            mbi.setBlockLimit(
                    blockInstruction.getBlockLimit().isPresent() ? blockInstruction.getBlockLimit().getAsInt() : null);
        }
        return this;
    }

    @Override
    public synchronized BlockList saveChanges() {
        this.blocks.forEach(b -> {
            String[] idSplit = b.getType().getId().split(":");
            this.file.set(new ConfigurationNode("BlockList", idSplit[0], idSplit[1]),
                          ShipsParsers.NODE_TO_BLOCK_INSTRUCTION, b);
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
        Set<BlockType> ignoreBlocks = ShipsPlugin
                .getPlugin()
                .getAllShipTypes()
                .parallelStream()
                .flatMap(type -> Stream.of(type.getIgnoredTypes()))
                .collect(Collectors.toSet());


        ConfigurationStream.ConfigurationFile file = this.getFile();
        Collection<BlockType> completedBefore = new HashSet<>();
        BlockTypes.OAK_SIGN
                .getLike()
                .forEach(w -> this.addToConfig(w, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        BlockTypes.OAK_WALL_SIGN
                .getLike()
                .forEach(w -> this.addToConfig(w, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        BlockTypes.PURPUR_BLOCK
                .getLike()
                .forEach(w -> this.addToConfig(w, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        BlockTypes.ANVIL
                .getLike()
                .forEach(w -> this.addToConfig(w, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        BlockTypes.BLACK_GLAZED_TERRACOTTA
                .getLike()
                .forEach(w -> this.addToConfig(w, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(CommonBlockGroups.SHULKER_BOX.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(CommonBlockGroups.FENCE.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(CommonBlockGroups.FENCE_GATE.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(CommonBlockGroups.DOOR.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(CommonBlockGroups.PISTON.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));

        this.addToConfig(BlockTypes.JUKEBOX, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.LEVER, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.LADDER, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.CRAFTING_TABLE, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.BRICKS, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.TNT, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.BOOKSHELF, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.GOLD_BLOCK, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.IRON_BLOCK, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.DIAMOND_BLOCK, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.NOTE_BLOCK, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.DISPENSER, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.LAPIS_BLOCK, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.GLASS, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.FURNACE, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.CHEST, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.ENDER_CHEST, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.TRAPPED_CHEST, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.GLASS, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.GLASS_PANE, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.FIRE, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.NETHERRACK, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.ENCHANTING_TABLE, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.REDSTONE_LAMP, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.EMERALD_BLOCK, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.BEACON, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.TRAPPED_CHEST, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.DAYLIGHT_DETECTOR, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.TRAPPED_CHEST, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.HOPPER, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.DROPPER, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.HAY_BLOCK, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.OBSERVER, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.REDSTONE_WIRE, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.CAULDRON, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.CAVE_AIR, CollideType.IGNORE, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.TALL_GRASS, CollideType.IGNORE, completedBefore, ignoreBlocks);

        this.addToConfig(BlockTypes.REDSTONE_WIRE, CollideType.IGNORE, completedBefore, ignoreBlocks);

        Stream
                .of(BlockGroups1V13.LOG.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.WOOD_PLANKS.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.BANNER.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.CARPET.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.WOOL.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.BUTTON.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.BED.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.CONCRETE.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.CONCRETE_POWDER.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.SLAB.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.STAIRS.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.POTTED_SAPLING.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.TORCH.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.STAINED_GLASS.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.STAINED_GLASS_PANE.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.TERRACOTTA.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.PRESSURE_PLATE.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));
        Stream
                .of(BlockGroups1V13.TRAP_DOOR.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.MATERIAL, completedBefore, ignoreBlocks));


        Stream
                .of(BlockGroups1V13.SAPLING.getGrouped())
                .forEach(t -> this.addToConfig(t, CollideType.IGNORE, completedBefore, ignoreBlocks));
        this.addToConfig(BlockTypes1V13.DANDELION, CollideType.IGNORE, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes1V13.KELP, CollideType.IGNORE, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes1V13.REPEATER, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes1V13.COMPARATOR, CollideType.MATERIAL, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.CAVE_AIR, CollideType.IGNORE, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.TALL_GRASS, CollideType.IGNORE, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.TALL_SEAGRASS, CollideType.IGNORE, completedBefore, ignoreBlocks);
        this.addToConfig(BlockTypes.SEAGRASS, CollideType.IGNORE, completedBefore, ignoreBlocks);

        TranslateCore
                .getPlatform()
                .getBlockTypes()
                .forEach(bt -> this.addToConfig(bt, CollideType.DETECT_COLLIDE, completedBefore, ignoreBlocks));
        file.save();
    }

    private void addToConfig(BlockType type,
                             CollideType collide,
                             Collection<? super BlockType> current,
                             Collection<BlockType> ignore) {
        if (current.stream().anyMatch(c -> c.equals(type))) {
            return;
        }
        String[] idSplit = type.getId().split(":");
        this.file.set(new ConfigurationNode("BlockList", idSplit[0], idSplit[1]),
                      ShipsParsers.NODE_TO_BLOCK_INSTRUCTION, new ModifiableBlockInstruction(type).setCollide(collide));
        current.add(type);
    }
}
