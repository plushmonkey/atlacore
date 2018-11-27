package com.plushnode.atlacore.game.ability.common.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SourceTypes {
    private List<SourceType> types = new ArrayList<>();

    private SourceTypes(SourceType type) {
        this.types.add(type);
    }

    public static SourceTypes of(SourceType type) {
        return new SourceTypes(type);
    }

    public SourceTypes and(SourceType type) {
        this.types.add(type);
        return this;
    }

    public SourceTypes and(SourceType... types) {
        this.types.addAll(Arrays.asList(types));
        return this;
    }

    public boolean contains(SourceType type) {
        return types.contains(type);
    }
}
