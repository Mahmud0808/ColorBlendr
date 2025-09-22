// SPDX-License-Identifier: BSD-3-Clause AND (GPL-3.0-or-later OR Apache-2.0)

package io.github.muntashirakon.adb;

import android.os.Build;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * This class provides useful functions and fields for ADB protocol details.
 */
// Copyright 2013 Cameron Gutman
final class AdbProtocol {
    /**
     * The length of the ADB message header
     */
    public static final int ADB_HEADER_LENGTH = 24;

    /**
     * SYNC(online, sequence, "")
     *
     * @deprecated Obsolete, no longer used. Never used on the client side.
     */
    public static final int A_SYNC = 0x434e5953;

    /**
     * CNXN is the connect message. No messages (except AUTH) are valid before this message is received.
     */
    public static final int A_CNXN = 0x4e584e43;

    /**
     * The payload sent with the CONNECT message.
     */
    public static final byte[] SYSTEM_IDENTITY_STRING_HOST = StringCompat.getBytes("host::\0", "UTF-8");

    /**
     * AUTH is the authentication message. It is part of the RSA public key authentication added in Android 4.2.2
     * ({@link Build.VERSION_CODES#JELLY_BEAN_MR1}).
     */
    public static final int A_AUTH = 0x48545541;

    /**
     * OPEN is the open stream message. It is sent to open a new stream on the target device.
     */
    public static final int A_OPEN = 0x4e45504f;

    /**
     * OKAY is a success message. It is sent when a write is processed successfully.
     */
    public static final int A_OKAY = 0x59414b4f;

    /**
     * CLSE is the close stream message. It is sent to close an existing stream on the target device.
     */
    public static final int A_CLSE = 0x45534c43;

    /**
     * WRTE is the write stream message. It is sent with a payload that is the data to write to the stream.
     */
    public static final int A_WRTE = 0x45545257;

    /**
     * STLS is the Stream-based TLS1.3 authentication method, added in Android 9 ({@link Build.VERSION_CODES#P}).
     */
    public static final int A_STLS = 0x534c5453;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({A_SYNC, A_CNXN, A_OPEN, A_OKAY, A_CLSE, A_WRTE, A_AUTH, A_STLS})
    private @interface Command {
    }

    /**
     * Original payload size
     */
    public static final int MAX_PAYLOAD_V1 = 4 * 1024;
    /**
     * Supported payload size since Android 7 (N)
     */
    public static final int MAX_PAYLOAD_V2 = 256 * 1024;
    /**
     * Supported payload size since Android 9 (P)
     */
    public static final int MAX_PAYLOAD_V3 = 1024 * 1024;
    /**
     * Maximum supported payload size is set to the original to support all APIs
     */
    public static final int MAX_PAYLOAD = MAX_PAYLOAD_V1;

    /**
     * The original version of the ADB protocol
     */
    public static final int A_VERSION_MIN = 0x01000000;
    /**
     * The new version of the ADB protocol introduced in Android 9 (P) with the introduction of TLS
     */
    public static final int A_VERSION_SKIP_CHECKSUM = 0x01000001;
    public static final int A_VERSION = A_VERSION_MIN;

    /**
     * The current version of the Stream-based TLS
     */
    public static final int A_STLS_VERSION_MIN = 0x01000000;
    public static final int A_STLS_VERSION = A_STLS_VERSION_MIN;

    /**
     * This authentication type represents a SHA1 hash to sign.
     */
    public static final int ADB_AUTH_TOKEN = 1;

    /**
     * This authentication type represents the signed SHA1 hash.
     */
    public static final int ADB_AUTH_SIGNATURE = 2;

    /**
     * This authentication type represents an RSA public key.
     */
    public static final int ADB_AUTH_RSAPUBLICKEY = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ADB_AUTH_TOKEN, ADB_AUTH_SIGNATURE, ADB_AUTH_RSAPUBLICKEY})
    private @interface AuthType {
    }

    public static int getMaxData(int api) {
        if (api >= Build.VERSION_CODES.P) {
            return MAX_PAYLOAD_V3;
        }
        if (api >= Build.VERSION_CODES.N) {
            return MAX_PAYLOAD_V2;
        }
        return MAX_PAYLOAD_V1;
    }

    public static int getProtocolVersion(int api) {
        if (api >= Build.VERSION_CODES.P) {
            return A_VERSION_SKIP_CHECKSUM;
        }
        return A_VERSION_MIN;
    }

    /**
     * This function performs a checksum on the ADB payload data.
     *
     * @param data The data
     * @return The checksum of the data
     */
    private static int getPayloadChecksum(@NonNull byte[] data) {
        return getPayloadChecksum(data, 0, data.length);
    }

    /**
     * This function performs a checksum on the ADB payload data.
     *
     * @param data   The data
     * @param offset The start offset in the data
     * @param length The number of bytes to take from the data
     * @return The checksum of the data
     */
    private static int getPayloadChecksum(@NonNull byte[] data, int offset, int length) {
        int checksum = 0;
        for (int i = offset; i < offset + length; ++i) {
            checksum += data[i] & 0xFF;
        }
        return checksum;
    }

    /**
     * This function generates an ADB message given the fields.
     *
     * @param command Command identifier constant
     * @param arg0    First argument
     * @param arg1    Second argument
     * @param data    The data
     * @return Byte array containing the message
     */
    @NonNull
    public static byte[] generateMessage(@Command int command, int arg0, int arg1, @Nullable byte[] data) {
        return generateMessage(command, arg0, arg1, data, 0, data == null ? 0 : data.length);
    }

    /**
     * This function generates an ADB message given the fields.
     *
     * @param command Command identifier constant
     * @param arg0    First argument
     * @param arg1    Second argument
     * @param data    The data
     * @param offset  The start offset in the data
     * @param length  The number of bytes to take from the data
     * @return Byte array containing the message
     */
    @NonNull
    public static byte[] generateMessage(@Command int command, int arg0, int arg1, @Nullable byte[] data, int offset, int length) {
        // Protocol as defined at https://github.com/aosp-mirror/platform_system_core/blob/6072de17cd812daf238092695f26a552d3122f8c/adb/protocol.txt
        // struct message {
        //     unsigned command;       // command identifier constant
        //     unsigned arg0;          // first argument
        //     unsigned arg1;          // second argument
        //     unsigned data_length;   // length of payload (0 is allowed)
        //     unsigned data_check;    // checksum of data payload
        //     unsigned magic;         // command ^ 0xffffffff
        // };

        ByteBuffer message;

        if (data != null) {
            message = ByteBuffer.allocate(ADB_HEADER_LENGTH + length).order(ByteOrder.LITTLE_ENDIAN);
        } else {
            message = ByteBuffer.allocate(ADB_HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        }

        message.putInt(command);
        message.putInt(arg0);
        message.putInt(arg1);

        if (data != null) {
            message.putInt(length);
            message.putInt(getPayloadChecksum(data, offset, length));
        } else {
            message.putInt(0);
            message.putInt(0);
        }

        message.putInt(~command);

        if (data != null) {
            message.put(data, offset, length);
        }

        return message.array();
    }

    /**
     * Generates a CONNECT message for a given API.
     * <p>
     * CONNECT(version, maxdata, "system-identity-string")
     *
     * @param api API version
     * @return Byte array containing the message
     */
    @NonNull
    public static byte[] generateConnect(int api) {
        return generateMessage(A_CNXN, getProtocolVersion(api), getMaxData(api), SYSTEM_IDENTITY_STRING_HOST);
    }

    /**
     * Generates an AUTH message with the specified type and payload.
     * <p>
     * AUTH(type, 0, "data")
     *
     * @param type Authentication type (see ADB_AUTH_* constants)
     * @param data The data
     * @return Byte array containing the message
     */
    @NonNull
    public static byte[] generateAuth(@AuthType int type, byte[] data) {
        return generateMessage(A_AUTH, type, 0, data);
    }

    /**
     * Generates an STLS message with default parameters.
     * <p>
     * STLS(version, 0, "")
     *
     * @return Byte array containing the message
     */
    @NonNull
    public static byte[] generateStls() {
        return generateMessage(A_STLS, A_STLS_VERSION, 0, null);
    }

    /**
     * Generates an OPEN stream message with the specified local ID and destination.
     * <p>
     * OPEN(local-id, 0, "destination")
     *
     * @param localId     A unique local ID identifying the stream
     * @param destination The destination of the stream on the target
     * @return Byte array containing the message
     */
    @NonNull
    public static byte[] generateOpen(int localId, @NonNull String destination) {
        ByteBuffer bbuf = ByteBuffer.allocate(destination.length() + 1);
        bbuf.put(StringCompat.getBytes(destination, "UTF-8"));
        bbuf.put((byte) 0);
        return generateMessage(A_OPEN, localId, 0, bbuf.array());
    }

    /**
     * Generates a WRITE stream message with the specified IDs and payload.
     * <p>
     * WRITE(local-id, remote-id, "data")
     *
     * @param localId  The unique local ID of the stream
     * @param remoteId The unique remote ID of the stream
     * @param data     The data
     * @param offset   The start offset in the data
     * @param length   The number of bytes to take from the data
     * @return Byte array containing the message
     */
    @NonNull
    public static byte[] generateWrite(int localId, int remoteId, byte[] data, int offset, int length) {
        return generateMessage(A_WRTE, localId, remoteId, data, offset, length);
    }

    /**
     * Generates a CLOSE stream message with the specified IDs.
     * <p>
     * CLOSE(local-id, remote-id, "")
     *
     * @param localId  The unique local ID of the stream
     * @param remoteId The unique remote ID of the stream
     * @return Byte array containing the message
     */
    @NonNull
    public static byte[] generateClose(int localId, int remoteId) {
        return generateMessage(A_CLSE, localId, remoteId, null);
    }

    /**
     * Generates an OKAY/READY message with the specified IDs.
     * <p>
     * READY(local-id, remote-id, "")
     *
     * @param localId  The unique local ID of the stream
     * @param remoteId The unique remote ID of the stream
     * @return Byte array containing the message
     */
    @NonNull
    public static byte[] generateReady(int localId, int remoteId) {
        return generateMessage(A_OKAY, localId, remoteId, null);
    }

    /**
     * This class provides an abstraction for the ADB message format.
     */
    static final class Message {
        /**
         * The command field of the message
         */
        @Command
        public final int command;
        /**
         * The arg0 field of the message
         */
        public final int arg0;
        /**
         * The arg1 field of the message
         */
        public final int arg1;
        /**
         * The payload length field of the message
         */
        public final int dataLength;
        /**
         * The checksum field of the message
         */
        public final int dataCheck;
        /**
         * The magic field of the message
         */
        public final int magic;
        /**
         * The payload of the message
         */
        public byte[] payload;

        /**
         * Read and parse an ADB message from the supplied input stream.
         * <p>
         * <b>Note:</b> If data is corrupted, the connection has to be closed immediately to avoid inconsistencies.
         *
         * @param in InputStream object to read data from
         * @return An AdbMessage object represented the message read
         * @throws IOException              If the stream fails while reading.
         * @throws StreamCorruptedException If data is corrupted.
         */
        @NonNull
        public static Message parse(@NonNull InputStream in, int protocolVersion, int maxData) throws IOException {
            ByteBuffer header = ByteBuffer.allocate(ADB_HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN);

            // Read header
            int dataRead = 0;
            do {
                int bytesRead = in.read(header.array(), dataRead, ADB_HEADER_LENGTH - dataRead);
                if (bytesRead < 0) {
                    throw new IOException("Stream closed");
                } else dataRead += bytesRead;
            } while (dataRead < ADB_HEADER_LENGTH);

            Message msg = new Message(header);

            // Validate header
            if (msg.command != (~msg.magic)) { // magic = cmd ^ 0xFFFFFFFF
                throw new StreamCorruptedException(String.format("Invalid header: Invalid magic 0x%x.", msg.magic));
            }
            if (msg.command != A_SYNC && msg.command != A_CNXN && msg.command != A_OPEN && msg.command != A_OKAY
                    && msg.command != A_CLSE && msg.command != A_WRTE && msg.command != A_AUTH
                    && msg.command != A_STLS) {
                throw new StreamCorruptedException(String.format("Invalid header: Invalid command 0x%x.", msg.command));
            }
            if (msg.dataLength < 0 || msg.dataLength > maxData) {
                throw new StreamCorruptedException(String.format("Invalid header: Invalid data length %d", msg.dataLength));
            }

            if (msg.dataLength == 0) {
                // No payload supplied, return immediately
                return msg;
            }

            // Read payload
            msg.payload = new byte[msg.dataLength];
            dataRead = 0;
            do {
                int bytesRead = in.read(msg.payload, dataRead, msg.dataLength - dataRead);
                if (bytesRead < 0) {
                    throw new IOException("Stream closed");
                } else dataRead += bytesRead;
            } while (dataRead < msg.dataLength);

            // Verify payload
            if ((protocolVersion <= A_VERSION_MIN || (msg.command == A_CNXN && msg.arg0 <= A_VERSION_MIN))
                    && getPayloadChecksum(msg.payload) != msg.dataCheck) {
                // Checksum verification failed
                throw new StreamCorruptedException("Invalid header: Checksum mismatched.");
            }

            return msg;
        }

        private Message(@NonNull ByteBuffer header) {
            command = header.getInt();
            arg0 = header.getInt();
            arg1 = header.getInt();
            dataLength = header.getInt();
            dataCheck = header.getInt();
            magic = header.getInt();
        }

        @NonNull
        @Override
        public String toString() {
            String tag;
            switch (command) {
                case A_SYNC:
                    tag = "SYNC";
                    break;
                case A_CNXN:
                    tag = "CNXN";
                    break;
                case A_OPEN:
                    tag = "OPEN";
                    break;
                case A_OKAY:
                    tag = "OKAY";
                    break;
                case A_CLSE:
                    tag = "CLSE";
                    break;
                case A_WRTE:
                    tag = "WRTE";
                    break;
                case A_AUTH:
                    tag = "AUTH";
                    break;
                case A_STLS:
                    tag = "STLS";
                    break;
                default:
                    tag = "????";
                    break;
            }
            return "Message{" +
                    "command=" + tag +
                    ", arg0=0x" + Integer.toHexString(arg0) +
                    ", arg1=0x" + Integer.toHexString(arg1) +
                    ", payloadLength=" + dataLength +
                    ", checksum=" + dataCheck +
                    ", magic=0x" + Integer.toHexString(magic) +
                    ", payload=" + Arrays.toString(payload) +
                    '}';
        }
    }

}
