package com.plushnode.atlacore.game.attribute;

public enum ModifierOperation {
    // Directly adds to the base value
    ADDITIVE,
    // This is summed with the others of this operation and then multiplied by the ADDITIVE result.
    SUMMED_MULTIPLICATIVE,
    // The base is multiplied by every MULTIPLICATIVE modifier after doing ADDITIVE and SUMMED_MULTIPLICATIVE.
    MULTIPLICATIVE
}
