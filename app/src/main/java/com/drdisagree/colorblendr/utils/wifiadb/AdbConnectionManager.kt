package com.drdisagree.colorblendr.utils.wifiadb

import android.content.Context
import android.os.Build
import android.sun.misc.BASE64Encoder
import android.sun.security.provider.X509Factory
import android.sun.security.x509.AlgorithmId
import android.sun.security.x509.CertificateAlgorithmId
import android.sun.security.x509.CertificateExtensions
import android.sun.security.x509.CertificateIssuerName
import android.sun.security.x509.CertificateSerialNumber
import android.sun.security.x509.CertificateSubjectName
import android.sun.security.x509.CertificateValidity
import android.sun.security.x509.CertificateVersion
import android.sun.security.x509.CertificateX509Key
import android.sun.security.x509.KeyIdentifier
import android.sun.security.x509.PrivateKeyUsageExtension
import android.sun.security.x509.SubjectKeyIdentifierExtension
import android.sun.security.x509.X500Name
import android.sun.security.x509.X509CertImpl
import android.sun.security.x509.X509CertInfo
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.spec.EncodedKeySpec
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import java.util.Random

class AdbConnectionManager private constructor(context: Context) : AbsAdbConnectionManager() {

    private var mPrivateKey: PrivateKey?
    private var mCertificate: Certificate?

    init {
        api = Build.VERSION.SDK_INT
        mPrivateKey = readPrivateKeyFromFile(context)
        mCertificate = readCertificateFromFile(context)
        if (mPrivateKey == null) {
            // Generate a new key pair
            val keySize = 2048
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(keySize, SecureRandom.getInstance("SHA1PRNG"))
            val generateKeyPair = keyPairGenerator.generateKeyPair()
            val publicKey = generateKeyPair.public
            mPrivateKey = generateKeyPair.private
            // Generate a new certificate
            val subject = "CN=$deviceName"
            val algorithmName = "SHA512withRSA"
            val expiryDate = System.currentTimeMillis() + 86400000
            val certificateExtensions = CertificateExtensions()
            certificateExtensions.set(
                "SubjectKeyIdentifier", SubjectKeyIdentifierExtension(
                    KeyIdentifier(publicKey).identifier
                )
            )
            val x500Name = X500Name(subject)
            val notBefore = Date()
            val notAfter = Date(expiryDate)
            certificateExtensions.set(
                "PrivateKeyUsage",
                PrivateKeyUsageExtension(notBefore, notAfter)
            )
            val certificateValidity = CertificateValidity(notBefore, notAfter)
            val x509CertInfo = X509CertInfo().apply {
                set("version", CertificateVersion(2))
                set(
                    "serialNumber",
                    CertificateSerialNumber(Random().nextInt() and Int.Companion.MAX_VALUE)
                )
                set("algorithmID", CertificateAlgorithmId(AlgorithmId.get(algorithmName)))
                set("subject", CertificateSubjectName(x500Name))
                set("key", CertificateX509Key(publicKey))
                set("validity", certificateValidity)
                set("issuer", CertificateIssuerName(x500Name))
                set("extensions", certificateExtensions)
            }
            val x509CertImpl = X509CertImpl(x509CertInfo).apply {
                sign(mPrivateKey, algorithmName)
            }
            mCertificate = x509CertImpl
            // Write files
            Companion.writePrivateKeyToFile(context, mPrivateKey!!)
            Companion.writeCertificateToFile(context, mCertificate!!)
        }
    }

    override fun getPrivateKey(): PrivateKey {
        return mPrivateKey!!
    }

    override fun getCertificate(): Certificate {
        return mCertificate!!
    }

    override fun getDeviceName(): String {
        return appContext.getString(R.string.app_name)
    }

    companion object {
        @Volatile
        private var INSTANCE: AbsAdbConnectionManager? = null

        @Throws(Exception::class)
        fun getInstance(context: Context): AbsAdbConnectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdbConnectionManager(context).also { INSTANCE = it }
            }
        }

        @Throws(IOException::class, CertificateException::class)
        private fun readCertificateFromFile(context: Context): Certificate? {
            val certFile = File(context.filesDir, "cert.pem")
            if (!certFile.exists()) return null
            FileInputStream(certFile).use { cert ->
                return CertificateFactory.getInstance("X.509").generateCertificate(cert)
            }
        }

        @Throws(CertificateEncodingException::class, IOException::class)
        private fun writeCertificateToFile(context: Context, certificate: Certificate) {
            val certFile = File(context.filesDir, "cert.pem")
            val encoder = BASE64Encoder()
            FileOutputStream(certFile).use { os ->
                os.write(X509Factory.BEGIN_CERT.toByteArray(StandardCharsets.UTF_8))
                os.write('\n'.code)
                encoder.encode(certificate.encoded, os)
                os.write('\n'.code)
                os.write(X509Factory.END_CERT.toByteArray(StandardCharsets.UTF_8))
            }
        }

        @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class)
        private fun readPrivateKeyFromFile(context: Context): PrivateKey? {
            val privateKeyFile = File(context.filesDir, "private.key")
            if (!privateKeyFile.exists()) return null
            val privKeyBytes = ByteArray(privateKeyFile.length().toInt())
            FileInputStream(privateKeyFile).use { `is` ->
                `is`.read(privKeyBytes)
            }
            val keyFactory = KeyFactory.getInstance("RSA")
            val privateKeySpec: EncodedKeySpec = PKCS8EncodedKeySpec(privKeyBytes)
            return keyFactory.generatePrivate(privateKeySpec)
        }

        @Throws(IOException::class)
        private fun writePrivateKeyToFile(context: Context, privateKey: PrivateKey) {
            val privateKeyFile = File(context.filesDir, "private.key")
            FileOutputStream(privateKeyFile).use { os ->
                os.write(privateKey.encoded)
            }
        }
    }
}
