package com.plushnode.atlacore;

public interface Task {
    void run();
    default void cancel() {

    }
}
