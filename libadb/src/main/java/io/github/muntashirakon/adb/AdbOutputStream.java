// SPDX-License-Identifier: GPL-3.0-or-later OR Apache-2.0

package io.github.muntashirakon.adb;

import java.io.IOException;
import java.io.OutputStream;

public class AdbOutputStream extends OutputStream {
    private final AdbStream mAdbStream;

    public AdbOutputStream(AdbStream adbStream) {
        this.mAdbStream = adbStream;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) (b & 0xFF)});
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        mAdbStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        mAdbStream.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}
