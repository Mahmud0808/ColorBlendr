// SPDX-License-Identifier: GPL-3.0-or-later OR Apache-2.0

package io.github.muntashirakon.adb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.bouncycastle.util.encoders.Base64;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Objects;

import javax.crypto.Cipher;

final class AndroidPubkey {
    /**
     * Size of an RSA modulus such as an encrypted block or a signature.
     */
    public static final int ANDROID_PUBKEY_MODULUS_SIZE = 2048 / 8;

    /**
     * Size of an encoded RSA key.
     */
    public static final int ANDROID_PUBKEY_ENCODED_SIZE = 3 * 4 + 2 * ANDROID_PUBKEY_MODULUS_SIZE;

    /**
     * Size of the RSA modulus in words.
     */
    public static final int ANDROID_PUBKEY_MODULUS_SIZE_WORDS = ANDROID_PUBKEY_MODULUS_SIZE / 4;

    /**
     * The RSA signature padding as an int array.
     */
    private static final int[] SIGNATURE_PADDING_AS_INT = new int[]{
            0x00, 0x01, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
            0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0x00,
            0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05, 0x00,
            0x04, 0x14
    };

    /**
     * The RSA signature padding as a byte array
     */
    private static final byte[] RSA_SHA_PKCS1_SIGNATURE_PADDING;

    static {
        RSA_SHA_PKCS1_SIGNATURE_PADDING = new byte[SIGNATURE_PADDING_AS_INT.length];

        for (int i = 0; i < RSA_SHA_PKCS1_SIGNATURE_PADDING.length; i++)
            RSA_SHA_PKCS1_SIGNATURE_PADDING[i] = (byte) SIGNATURE_PADDING_AS_INT[i];
    }

    /**
     * Signs the ADB SHA1 payload with the private key of this object.
     *
     * @param privateKey Private key to sign with
     * @param payload    SHA1 payload to sign
     * @return Signed SHA1 payload
     * @throws GeneralSecurityException If signing fails
     */
    // Taken from adb_auth_sign
    @NonNull
    public static byte[] adbAuthSign(@NonNull PrivateKey privateKey, byte[] payload)
            throws GeneralSecurityException {
        Cipher c = Cipher.getInstance("RSA/ECB/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, privateKey);
        c.update(RSA_SHA_PKCS1_SIGNATURE_PADDING);
        return c.doFinal(payload);
    }

    /**
     * Converts a standard RSAPublicKey object to the special ADB format. Available since 4.2.2.
     *
     * @param publicKey RSAPublicKey object to convert
     * @param name      Name without null terminator
     * @return Byte array containing the converted RSAPublicKey object
     */
    @NonNull
    public static byte[] encodeWithName(@NonNull RSAPublicKey publicKey, @NonNull String name)
            throws InvalidKeyException {
        int pkeySize = 4 * (int) Math.ceil(ANDROID_PUBKEY_ENCODED_SIZE / 3.0);
        try (ByteArrayNoThrowOutputStream bos = new ByteArrayNoThrowOutputStream(pkeySize + name.length() + 2)) {
            bos.write(Base64.encode(encode(publicKey)));
            bos.write(getUserInfo(name));
            return bos.toByteArray();
        }
    }

    // Taken from get_user_info except that a custom name is used instead of host@user
    @VisibleForTesting
    @NonNull
    static byte[] getUserInfo(@NonNull String name) {
        return StringCompat.getBytes(String.format(" %s\u0000", name), "UTF-8");
    }

    // https://android.googlesource.com/platform/system/core/+/e797a5c75afc17024d0f0f488c130128fcd704e2/libcrypto_utils/android_pubkey.cpp
    // typedef struct RSAPublicKey {
    //     uint32_t modulus_size_words;                     // Modulus length. This must be ANDROID_PUBKEY_MODULUS_SIZE.
    //     uint32_t n0inv;                                  // Precomputed montgomery parameter: -1 / n[0] mod 2^32
    //     uint8_t modulus[ANDROID_PUBKEY_MODULUS_SIZE];    // RSA modulus as a little-endian array.
    //     uint8_t rr[ANDROID_PUBKEY_MODULUS_SIZE];         // Montgomery parameter R^2 as a little-endian array.
    //     uint32_t exponent;                               // RSA modulus: 3 or 65537
    // } RSAPublicKey;

    /**
     * Allocates a new {@link RSAPublicKey} object, decodes a public RSA key stored in Android's custom binary format,
     * and sets the key parameters. The resulting key can be used with the standard Java cryptography API to perform
     * public operations.
     *
     * @param androidPubkey Public RSA key in Android's custom binary format. The size of the key must be at least
     *                      {@link #ANDROID_PUBKEY_ENCODED_SIZE}
     * @return {@link RSAPublicKey} object
     */
    @NonNull
    public static RSAPublicKey decode(@NonNull byte[] androidPubkey)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger n;
        BigInteger e;

        // Check size is large enough and the modulus size is correct.
        if (androidPubkey.length < ANDROID_PUBKEY_ENCODED_SIZE) {
            throw new InvalidKeyException("Invalid key length");
        }
        ByteBuffer keyStruct = ByteBuffer.wrap(androidPubkey).order(ByteOrder.LITTLE_ENDIAN);
        int modulusSize = keyStruct.getInt();
        if (modulusSize != ANDROID_PUBKEY_MODULUS_SIZE_WORDS) {
            throw new InvalidKeyException("Invalid modulus length.");
        }

        // Convert the modulus to big-endian byte order as expected by BN_bin2bn.
        byte[] modulus = new byte[ANDROID_PUBKEY_MODULUS_SIZE];
        keyStruct.position(8);
        keyStruct.get(modulus);
        n = new BigInteger(1, swapEndianness(modulus));

        // Read the exponent.
        keyStruct.position(520);
        e = BigInteger.valueOf(keyStruct.getInt());

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
        return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
    }

    /**
     * Encodes the given key in the Android RSA public key binary format.
     *
     * @return Public RSA key in Android's custom binary format. The size of the key should be at least
     * {@link #ANDROID_PUBKEY_ENCODED_SIZE}
     */
    @NonNull
    public static byte[] encode(@NonNull RSAPublicKey publicKey) throws InvalidKeyException {
        BigInteger r32;
        BigInteger n0inv;
        BigInteger rr;

        if (publicKey.getModulus().toByteArray().length < ANDROID_PUBKEY_MODULUS_SIZE) {
            throw new InvalidKeyException("Invalid key length " + publicKey.getModulus().toByteArray().length);
        }

        ByteBuffer keyStruct = ByteBuffer.allocate(ANDROID_PUBKEY_ENCODED_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        // Store the modulus size.
        keyStruct.putInt(ANDROID_PUBKEY_MODULUS_SIZE_WORDS); // modulus_size_words

        // Compute and store n0inv = -1 / N[0] mod 2^32.
        r32 = BigInteger.ZERO.setBit(32); // r32 = 2^32
        n0inv = publicKey.getModulus().mod(r32); // n0inv = N[0] mod 2^32
        n0inv = n0inv.modInverse(r32); // n0inv = 1/n0inv mod 2^32
        n0inv = r32.subtract(n0inv);  // n0inv = 2^32 - n0inv
        keyStruct.putInt(n0inv.intValue()); // n0inv

        // Store the modulus.
        keyStruct.put(Objects.requireNonNull(BigEndianToLittleEndianPadded(ANDROID_PUBKEY_MODULUS_SIZE, publicKey.getModulus())));

        // Compute and store rr = (2^(rsa_size)) ^ 2 mod N.
        rr = BigInteger.ZERO.setBit(ANDROID_PUBKEY_MODULUS_SIZE * 8); // rr = 2^(rsa_size)
        rr = rr.modPow(BigInteger.valueOf(2), publicKey.getModulus()); // rr = rr^2 mod N
        keyStruct.put(Objects.requireNonNull(BigEndianToLittleEndianPadded(ANDROID_PUBKEY_MODULUS_SIZE, rr)));

        // Store the exponent.
        keyStruct.putInt(publicKey.getPublicExponent().intValue()); // exponent

        return keyStruct.array();
    }

    @Nullable
    private static byte[] BigEndianToLittleEndianPadded(int len, @NonNull BigInteger in) {
        byte[] out = new byte[len];
        byte[] bytes = swapEndianness(in.toByteArray());  // Convert big endian -> little endian
        int num_bytes = bytes.length;
        if (len < num_bytes) {
            if (!fitsInBytes(bytes, num_bytes, len)) {
                return null;
            }
            num_bytes = len;
        }
        System.arraycopy(bytes, 0, out, 0, num_bytes);
        return out;
    }

    static boolean fitsInBytes(@NonNull byte[] bytes, int num_bytes, int len) {
        byte mask = 0;
        for (int i = len; i < num_bytes; i++) {
            mask |= bytes[i];
        }
        return mask == 0;
    }

    @NonNull
    private static byte[] swapEndianness(@NonNull byte[] bytes) {
        int len = bytes.length;
        byte[] out = new byte[len];
        for (int i = 0; i < len; ++i) {
            out[i] = bytes[len - i - 1];
        }
        return out;
    }
}
