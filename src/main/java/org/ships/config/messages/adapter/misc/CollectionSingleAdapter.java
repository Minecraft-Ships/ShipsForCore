package org.ships.config.messages.adapter.misc;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.*;
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
    public Set<String> examples() {
        return this.adapters
                .parallelStream()
                .flatMap(ma -> ma.examples().parallelStream())
                .map(example -> "!!" + example + "[1]!!")
                .collect(Collectors.toSet());
    }

    @Override
    @Deprecated
    public AText process(@NotNull Collection<T> obj) {
        return this.process(obj, AText.ofPlain(this.examples().iterator().next()));
    }

    @Override
    public AText process(@NotNull Collection<T> obj, @NotNull AText message) {
        String plain = message.toPlain();
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
        Optional<AText> opReplacement = this.adapters.parallelStream().filter(ma -> {
            boolean check = ma.containsAdapter("%" + adapterText + "%");
            TranslateCore
                    .getConsole()
                    .sendMessage(AText
                                         .ofPlain("MA: " + ma.adapterText() + ": Check:  " + check)
                                         .withColour(NamedTextColours.RED));
            return check;
        }).map(ma -> {
            AText process = ma.process(indexedValue, AText.ofPlain("%" + adapterText + "%"));
            TranslateCore
                    .getConsole()
                    .sendMessage(AText
                                         .ofPlain("MA: " + ma.adapterText() + ": Value: " + process.toPlain())
                                         .withColour(NamedTextColours.RED));
            return process;
        }).findAny();

        TranslateCore.getConsole().sendMessage(AText.ofPlain("Message: " + message.toPlain()));
        TranslateCore.getConsole().sendMessage(AText.ofPlain("Found: " + opReplacement.map(AText::toPlain)));
        TranslateCore.getConsole().sendMessage(AText.ofPlain("Changing: " + adaptingWithFormat));

        AText result = opReplacement.map(text -> message.withAllAs(adaptingWithFormat, text)).orElse(message);
        TranslateCore.getConsole().sendMessage(AText.ofPlain("Result: ").append(result));
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
    public boolean containsAdapter(AText text) {
        return this.containsAdapter(text.toPlain());
    }

    public String adapterText(MessageAdapter<T> adapter, int index) {
        return adapter.adapterText() + "[" + index + "]";
    }

    public String adapterTextFormat(MessageAdapter<T> adapter, int index) {
        return "!!" + this.adapterText(adapter, index) + "!!";
    }
}
