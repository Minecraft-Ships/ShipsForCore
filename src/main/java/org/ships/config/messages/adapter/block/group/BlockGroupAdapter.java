package org.ships.config.messages.adapter.block.group;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.core.utils.ComponentUtils;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.grouptype.BlockGroup;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class BlockGroupAdapter implements MessageAdapter<Collection<BlockType>> {

    private final String name;
    private final Function<Identifiable, Component> toString;

    public BlockGroupAdapter(String name, Function<Identifiable, Component> failSafe) {
        this.toString = failSafe;
        this.name = name;
    }

    @Override
    public String adapterText() {
        return this.name;
    }

    @Override
    public Class<?> adaptingType() {
        return Collection.class;
    }

    @Override
    public Set<String> examples() {
        return Set.of("%" + this.name + "% is in the way");
    }

    @Override
    public Collection<AdapterCategory<Collection<BlockType>>> categories() {
        return List.of(AdapterCategories.BLOCK_GROUP);
    }

    @Override
    public Component processMessage(@NotNull Collection<BlockType> obj) {
        if (obj.isEmpty()) {
            return Component.empty();
        }
        var blockTypes = new ArrayList<>(obj);
        BlockType blockType = obj.iterator().next();
        List<BlockGroup> potentialBlockGroups = blockType
                .getBlockGroups()
                .filter(blockGroup -> obj.parallelStream().anyMatch(type -> type.getBlockGroups().anyMatch(group -> group.equals(blockGroup))))
                .collect(Collectors.toList());

        var blockGroups = getValidBlockGroups(blockTypes, potentialBlockGroups);

        if (blockTypes.isEmpty() && blockGroups.size() == 1) {
            return this.toString.apply(blockGroups.iterator().next());
        }
        blockTypes.sort(Comparator.comparing(type -> ComponentUtils.toPlain(this.toString.apply(type))));

        Stream<Component> components = blockTypes.stream().map(this.toString);
        components = Stream.concat(components, blockGroups.stream().map(this.toString));
        return Component.join(JoinConfiguration.builder().separator(Component.text(", ")).build(),
                              components.collect(Collectors.toList()));
    }

    private Collection<BlockGroup> getValidBlockGroups(Collection<BlockType> types, Collection<BlockGroup> groups) {
        Optional<BlockGroup> allMatchGroup = groups
                .parallelStream()
                .filter(group -> group.getBlocks().size() == types.size())
                .filter(group -> types.containsAll(group.getBlocks()))
                .findAny();
        if (allMatchGroup.isPresent()) {
            types.clear();
            return List.of(allMatchGroup.get());
        }

        List<BlockGroup> allMatchGroups = groups
                .parallelStream()
                .filter(group -> types.containsAll(group.getBlocks()))
                .sorted(Comparator.comparing(group -> group.getBlocks().size()))
                .collect(Collectors.toList());
        Collection<BlockGroup> ret = new ArrayList<>();
        for (BlockGroup group : allMatchGroups) {
            if (types.containsAll(group.getBlocks())) {
                types.removeAll(group.getBlocks());
                ret.add(group);
            }
        }
        return ret;


    }


}
