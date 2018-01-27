package com.plushnode.atlacore.store.sql;

@FunctionalInterface
public interface SqlRunnable<T> {
    T run() throws Exception;
}
