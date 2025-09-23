// SPDX-License-Identifier: GPL-3.0-or-later OR Apache-2.0

package io.github.muntashirakon.adb;

import android.os.Build;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

final class StringCompat {
    @NonNull
    public static byte[] getBytes(@NonNull String text, @NonNull String charsetName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return text.getBytes(Charset.forName(charsetName));
        }
        try {
            return text.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw (IllegalCharsetNameException) new IllegalCharsetNameException("Illegal charset " + charsetName)
                    .initCause(e);
        }
    }
}
