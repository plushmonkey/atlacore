package com.plushnode.atlacore.util;

public interface Task {
    void run();
    default void cancel() {

    }
}
