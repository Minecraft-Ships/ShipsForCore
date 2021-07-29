package org.ships.config.messages.adapter.misc;

import org.core.adventureText.AText;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.*;
import java.util.stream.Collectors;

public class CollectionSingleAdapter<T> implements MessageAdapter<Collection<T>> {

    private final Collection<MessageAdapter<T>> adapters;

    public CollectionSingleAdapter(Collection<MessageAdapter<T>> collection) {
        if(collection.isEmpty()){
            throw new IllegalArgumentException("Collection Single Adapter must have adapters, not a empty list");
        }
        this.adapters = collection;
    }

    @Override
    @Deprecated
    public String adapterText() {
        return "[0]";
    }

    public String adapterText(MessageAdapter<T> adapter, int index){
        return adapter.adapterTextFormat() + "[" + index + "]";
    }

    @Override
    @Deprecated
    public String adapterTextFormat() {
        return "!!" + adapterText() + "!!";
    }

    public String adapterTextFormat(MessageAdapter<T> adapter, int index){
        return "!!" + adapterText(adapter, index) + "!!";
    }

    @Override
    public Set<String> examples() {
        return this
                .adapters
                .parallelStream()
                .flatMap(ma -> ma.examples().parallelStream())
                .map(example -> "!!" + example + "[1]!!")
                .collect(Collectors.toSet());
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
        String adaptingWithFormat = plain.substring(before, target);
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

        return this
                .adapters
                .parallelStream()
                .anyMatch(ma -> ma.containsAdapter(adapterText));
    }

    @Override
    public boolean containsAdapter(AText text) {
        return containsAdapter(text.toPlain());
    }

    @Override
    public AText process(AText message, Collection<T> obj) {
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
        String adaptingWithFormat = plain.substring(before, target);
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
        int finalIndex = index;

        Optional<AText> opReplacement = this
                .adapters
                .parallelStream()
                .filter(ma -> ma.containsAdapter(adapterText))
                .map(ma -> ma.process(AText.ofPlain(adapterText), list.get(finalIndex)))
                .findAny();

        return opReplacement
                .map(text -> message.withAllAs("!!" + adaptingWithFormat + "[" + finalIndex + "]!!", text))
                .orElse(message);

    }
}
