package com.plushnode.atlacore.store.sql;

import java.io.*;
import java.util.UUID;

public final class SqlUtil {
    private SqlUtil() {

    }

    public static ByteArrayInputStream uuidToStream(UUID uuid) throws IOException {
        return new ByteArrayInputStream(uuidToBytes(uuid));
    }

    public static UUID streamToUuid(InputStream stream) throws IOException {
        byte[] uuidBytes = new byte[16];

        stream.read(uuidBytes);

        ByteArrayInputStream ba = new ByteArrayInputStream(uuidBytes);
        DataInputStream is = new DataInputStream(ba);

        return new UUID(is.readLong(), is.readLong());
    }

    public static byte[] uuidToBytes(UUID uuid) throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream(16);
        DataOutputStream os = new DataOutputStream(ba);

        os.writeLong(uuid.getMostSignificantBits());
        os.writeLong(uuid.getLeastSignificantBits());

        return ba.toByteArray();
    }
}
