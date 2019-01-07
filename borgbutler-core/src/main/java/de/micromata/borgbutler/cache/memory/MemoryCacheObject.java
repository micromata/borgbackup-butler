package de.micromata.borgbutler.cache.memory;

import lombok.Getter;

public abstract class MemoryCacheObject<I> {
    @Getter
    private I identifier;
    long lastAcess;

    abstract protected boolean matches(I identifier);

    abstract protected int getSize();

    boolean _matches(I identifier) {
        if (this.identifier == null || identifier == null) {
            return false;
        }
        if (matches(identifier)) {
            lastAcess = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public MemoryCacheObject(I identifier) {
        this.identifier = identifier;
        lastAcess = System.currentTimeMillis();
    }

}
