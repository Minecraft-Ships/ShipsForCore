package org.ships.config.messages.adapter.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.utils.ComponentUtils;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CollectionSingleAdapter<T> implements MessageAdapter<Collection<T>> {

    private final Collection<? extends MessageAdapter<T>> adapters;

    public CollectionSingleAdapter(Collection<? extends MessageAdapter<T>> collection) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Collection Single Adapter must have adapters, not a empty list");
        }
        this.adapters = collection;
    }

    @Override
    @Deprecated
    public String adapterText() {
        return "[0]";
    }

    @Override
    public Class<?> adaptingType() {
        return Collection.class;
    }

    @Override
    public Set<String> examples() {
        return this.adapters
                .parallelStream()
                .flatMap(ma -> ma.examples().parallelStream())
                .map(example -> "!!" + example + "[1]!!")
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<AdapterCategory<Collection<T>>> categories() {
        return this.adapters
                .parallelStream()
                .flatMap(adapter -> adapter.categories().stream())
                .map(adapter -> (AdapterCategory<Collection<T>>) adapter)
                .collect(Collectors.toList());
    }

    @Override
    public Component processMessage(@NotNull Collection<T> obj) {
        return this.processMessage(obj, Component.text(this.examples().iterator().next()));
    }

    @Override
    public Component processMessage(@NotNull Collection<T> obj, @NotNull Component message) {
        String plain = ComponentUtils.toPlain(message);
        int target = 0;
        Integer before = null;
        boolean was = false;
        for (; target < plain.length(); target++) {
            char at = plain.charAt(target);
            if (at != '!') {
                was = false;
                continue;
            }
            if (before != null) {
                if (was) {
                    break;
                }
                was = true;
                continue;
            }
            if (was) {
                was = false;
                before = target - 1;
                continue;
            }
            was = true;
        }
        if (before == null || target == plain.length() && !plain.endsWith("!!")) {
            return message;
        }
        String adaptingWithFormat = plain.substring(before, target + 1);
        String adapting = adaptingWithFormat.substring(2, adaptingWithFormat.length() - 2);
        if (!adapting.endsWith("]")) {
            return message;
        }
        int startAt = adapting.length() - 1;
        for (; startAt >= -1; startAt--) {
            if (startAt == -1) {
                return message;
            }
            if (adapting.charAt(startAt) == '[') {
                break;
            }
        }
        int index = 0;
        try {
            index = Integer.parseInt(adapting.substring(startAt, adapting.length() - 1));
        } catch (NumberFormatException ignore) {

        }
        List<T> list = new ArrayList<>(obj);
        String adapterText = adapting.substring(0, startAt);
        T indexedValue = list.get(index);
        Optional<Component> opReplacement = this.adapters.parallelStream().filter(ma -> {
            boolean check = ma.containsAdapter("%" + adapterText + "%");
            TranslateCore
                    .getConsole()
                    .sendMessage(
                            Component.text("MA: " + ma.adapterText() + ": Check:  " + check).color(NamedTextColor.RED));
            return check;
        }).map(ma -> {
            Component process = ma.processMessage(indexedValue, Component.text("%" + adapterText + "%"));
            TranslateCore
                    .getConsole()
                    .sendMessage(Component
                                         .text("MA: " + ma.adapterText() + ": Value: " + ComponentUtils.toPlain(
                                                 process))
                                         .color(NamedTextColor.RED));
            return process;
        }).findAny();

        TranslateCore.getConsole().sendMessage(Component.text("Message: ").append(message));
        TranslateCore
                .getConsole()
                .sendMessage(Component.text("Found: ").append(opReplacement.orElse(Component.empty())));
        TranslateCore.getConsole().sendMessage(Component.text("Changing: " + adaptingWithFormat));

        Component result = opReplacement
                .map(text -> message.replaceText(TextReplacementConfig
                                                         .builder()
                                                         .match(Pattern.compile(adaptingWithFormat,
                                                                                Pattern.CASE_INSENSITIVE))
                                                         .replacement(text)
                                                         .build()))
                .orElse(message);
        TranslateCore.getConsole().sendMessage(Component.text("Result: ").append(result));
        return result;

    }

    @Override
    @Deprecated
    public String adapterTextFormat() {
        return "!!" + this.adapterText() + "!!";
    }

    @Override
    public boolean containsAdapter(String plain) {
        int target = 0;
        Integer before = null;
        boolean was = false;
        for (; target < plain.length(); target++) {
            char at = plain.charAt(target);
            if (at != '!') {
                was = false;
                continue;
            }
            if (before != null) {
                if (was) {
                    break;
                }
                was = true;
                continue;
            }
            if (was) {
                was = false;
                before = target - 1;
                continue;
            }
            was = true;
        }
        if (before == null || target == plain.length() && !plain.endsWith("!!")) {
            return false;
        }
        String adaptingWithFormat = plain.substring(before, target + 1);
        String adapting = adaptingWithFormat.substring(2, adaptingWithFormat.length() - 2);
        if (!adapting.endsWith("]")) {
            return false;
        }
        int startAt = adapting.length() - 1;
        for (; startAt >= -1; startAt--) {
            if (startAt == -1) {
                return false;
            }
            if (adapting.charAt(startAt) == '[') {
                break;
            }
        }
        String adapterText = adapting.substring(0, startAt);

        return this.adapters.parallelStream().anyMatch(ma -> ma.containsAdapter(adapterText));
    }

    @Override
    @Deprecated
    public boolean containsAdapter(AText text) {
        return this.containsAdapter(text.toPlain());
    }

    @Override
    public boolean containsAdapter(@NotNull Component component) {
        return MessageAdapter.super.containsAdapter(component);
    }

    public String adapterText(MessageAdapter<T> adapter, int index) {
        return adapter.adapterText() + "[" + index + "]";
    }

    public String adapterTextFormat(MessageAdapter<T> adapter, int index) {
        return "!!" + this.adapterText(adapter, index) + "!!";
    }
}
