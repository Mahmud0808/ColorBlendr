// SPDX-License-Identifier: GPL-3.0-or-later OR Apache-2.0

package io.github.muntashirakon.adb;

import java.io.IOException;
import java.io.InputStream;

public class AdbInputStream extends InputStream {
    public AdbStream mAdbStream;

    public AdbInputStream(AdbStream adbStream) {
        this.mAdbStream = adbStream;
    }

    @Override
    public int read() throws IOException {
        byte[] bytes = new byte[1];
        if (read(bytes) == -1) {
            return -1;
        }
        return bytes[0];
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (mAdbStream.isClosed()) return -1;
        return mAdbStream.read(b, off, len);
    }

    @Override
    public void close() {
    }

    @Override
    public int available() throws IOException {
        return mAdbStream.available();
    }
}
