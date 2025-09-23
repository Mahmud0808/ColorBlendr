// SPDX-License-Identifier: GPL-3.0-or-later OR Apache-2.0

package io.github.muntashirakon.adb;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.NonNull;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509TrustManager;

final class SslUtils {
    private static boolean customConscrypt = false;
    private static SSLContext sslContext;

    public static boolean isCustomConscrypt() {
        return customConscrypt;
    }

    @SuppressLint("TrulyRandom") // The users are already instructed to fix this issue
    @NonNull
    public static SSLContext getSslContext(KeyPair keyPair) throws NoSuchAlgorithmException, KeyManagementException {
        if (sslContext != null) {
            return sslContext;
        }
        try {
            Class<?> providerClass = Class.forName("org.conscrypt.OpenSSLProvider");
            Provider openSslProvder = (Provider) providerClass.getDeclaredConstructor().newInstance();
            sslContext = SSLContext.getInstance("TLSv1.3", openSslProvder);
            customConscrypt = true;
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (Throwable e) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // Custom error message to inform user that they should use custom Conscrypt library.
                throw new NoSuchAlgorithmException("TLSv1.3 isn't supported on your platform. Use custom Conscrypt library instead.");
            }
            sslContext = SSLContext.getInstance("TLSv1.3");
            customConscrypt = false;
        }
        System.out.println("Using " + (customConscrypt ? "custom" : "default") + " TLSv1.3 provider...");
        sslContext.init(new KeyManager[]{getKeyManager(keyPair)},
                new X509TrustManager[]{getAllAcceptingTrustManager()},
                new SecureRandom());
        return sslContext;
    }

    @NonNull
    private static KeyManager getKeyManager(KeyPair keyPair) {
        return new X509ExtendedKeyManager() {
            private final String mAlias = "key";

            @Override
            public String[] getClientAliases(String keyType, Principal[] issuers) {
                return null;
            }

            @Override
            public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
                for (String keyType : keyTypes) {
                    if (keyType.equals("RSA")) return mAlias;
                }
                return null;
            }

            @Override
            public String[] getServerAliases(String keyType, Principal[] issuers) {
                return null;
            }

            @Override
            public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
                return null;
            }

            @Override
            public X509Certificate[] getCertificateChain(String alias) {
                if (this.mAlias.equals(alias)) {
                    return new X509Certificate[]{(X509Certificate) keyPair.getCertificate()};
                }
                return null;
            }

            @Override
            public PrivateKey getPrivateKey(String alias) {
                if (this.mAlias.equals(alias)) {
                    return keyPair.getPrivateKey();
                }
                return null;
            }
        };
    }

    @SuppressLint("TrustAllX509TrustManager") // Accept all certificates
    @NonNull
    private static X509TrustManager getAllAcceptingTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }
}
