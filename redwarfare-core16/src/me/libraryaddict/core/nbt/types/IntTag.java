package me.libraryaddict.core.nbt.types;

/**
 * Copyright Mojang AB.
 * <p>
 * Don't do evil.
 */

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends Tag {
    public int data;

    public IntTag(String name) {
        super(name);
    }

    public IntTag(String name, int data) {
        super(name);
        this.data = data;
    }

    @Override
    public Tag copy() {
        return new IntTag(getName(), data);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            IntTag o = (IntTag) obj;
            return data == o.data;
        }
        return false;
    }

    public byte getId() {
        return TAG_Int;
    }

    void load(DataInput dis) throws IOException {
        data = dis.readInt();
    }

    public String toString() {
        return "" + data;
    }

    void write(DataOutput dos) throws IOException {
        dos.writeInt(data);
    }

}
