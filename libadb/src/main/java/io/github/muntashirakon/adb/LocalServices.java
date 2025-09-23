// SPDX-License-Identifier: GPL-3.0-or-later OR Apache-2.0

package io.github.muntashirakon.adb;

import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;


/**
 * Local services extracted from the <a href="https://cs.android.com/android/platform/superproject/+/master:packages/modules/adb/client/commandline.cpp">ADB client</a>
 * for easy access.
 */
public class LocalServices {
    static final int SERVICE_FIRST = 1;

    public static final int SHELL = 1;
    /**
     * Remount the device's filesystem in read-write mode, instead of read-only. This is usually necessary before
     * performing an {@link #SYNC} request. This request may not succeed on certain builds which do not allow that.
     * <p>
     * This essentially executes {@code /system/bin/remount} command. Additional arguments such as {@code -R} can be
     * passed too.
     */
    public static final int REMOUNT = 2;
    public static final int FILE = 3;
    public static final int TCP_CONNECT = 4;
    public static final int LOCAL_UNIX_SOCKET = 5;
    public static final int LOCAL_UNIX_SOCKET_RESERVED = 6;
    public static final int LOCAL_UNIX_SOCKET_ABSTRACT = 7;
    public static final int LOCAL_UNIX_SOCKET_FILE_SYSTEM = 8;
    /**
     * Receive snapshots of the framebuffer. It requires sufficient privileges (or the connection is closed immediately)
     * but works as follows:
     * <p>
     * After an {@link AdbStream} is opened, ADB daemon sends a 16-byte binary structure containing the following fields
     * (little-endian format):
     * <pre>
     * uint32_t depth;     // framebuffer depth = 16
     * uint32_t size;      // framebuffer size in bytes = 2 * width * height
     * uint32_t width;     // framebuffer width in pixels
     * uint32_t height;    // framebuffer height in pixels
     * </pre>
     * After that, each time a snapshot is wanted, one byte should be sent through the channel, which will trigger the
     * daemon to send {@code size} bytes of framebuffer data.
     */
    public static final int FRAMEBUFFER = 9;
    /**
     * Connects to the JDWP thread running in the VM of process PID (specified as an argument).
     */
    public static final int CONNECT_JDWP = 10;
    /**
     * Receive the list of JDWP PIDs periodically. The format of the returned data is the following (in order):
     * <ol>
     * <li> {@code hex4}: The length of all content as a 4-char hexadecimal string i.e. {@code %04zx}.
     * <li> {@code content}: A series of ASCII lines of the following format:
     *  <pre>
     *  &lt;pid&gt; "\n"
     *  </pre>
     * </ol>
     * This service is used by DDMS to know which debuggable processes are running on the device/emulator.
     * <p>
     * Note that there is no single-shot service to retrieve the list only once.
     */
    public static final int TRACK_JDWP = 11;
    public static final int SYNC = 12;
    /**
     * Reverse socket connections from the device running ADB daemon to this client. This should not be used if both
     * the ADB daemon and the client are in the same device.
     * <p>
     * It takes an additional argument called {@code forward-command}. It can be one of the following:
     * <ul>
     * <li> {@code list-forward}: List all forwarded connections from the device
     *   This returns something that looks like the following:
     *   <ol>
     *   <li> {@code hex4}: The length of the payload, as 4 hexadecimal chars i.e. {@code %04zx}.
     *   <li> {@code payload}: A series of lines of the following format:
     *     <pre>
     *     host " " &lt;local&gt; " " &lt;remote&gt; "\n"
     *     </pre>
     *     Where &lt;local&gt;  is the device-specific endpoint (e.g. {@code tcp:9000}), and &lt;remote&gt; is the
     *     client-specific endpoint.
     *   </ol>
     * <li> forward:<local>;<remote>
     * <li> forward:norebind:<local>;<remote>
     * <li> killforward-all
     * <li> killforward:<local>
     * </ul>
     */
    public static final int REVERSE = 13;
    /**
     * Backup some or all packages installed in the device. For this to work, {@code allowBackup=true} must be present
     * in the application section of the AndroidManifest.xml of the app.
     * <p>
     * It takes additional arguments which can be one of the following:
     * <ul>
     * <li>List of packages (as array)
     * <li>{@code -all}
     * <li>{@code -shared}
     * </ul>
     * Output is a stream which is in zlib format with 24 bytes at the front (if unencrypted).
     */
    public static final int BACKUP = 14;
    /**
     * Restore a backup. Input is a stream which is in zlib format with 24 bytes at the front (if unencrypted).
     */
    public static final int RESTORE = 15;

    static final int SERVICE_LAST = 15;

    @IntDef({
            SHELL,
            REMOUNT,
            FILE,
            TCP_CONNECT,
            LOCAL_UNIX_SOCKET,
            LOCAL_UNIX_SOCKET_RESERVED,
            LOCAL_UNIX_SOCKET_ABSTRACT,
            LOCAL_UNIX_SOCKET_FILE_SYSTEM,
            FRAMEBUFFER,
            CONNECT_JDWP,
            TRACK_JDWP,
            SYNC,
            REVERSE,
            BACKUP,
            RESTORE,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Services {
    }

    @NonNull
    static String getServiceName(@Services int service) {
        switch (service) {
            case SHELL:
                return "shell:";
            case CONNECT_JDWP:
                return "jdwp:";
            case FILE:
                return "dev:";
            case FRAMEBUFFER:
                return "framebuffer:";
            case LOCAL_UNIX_SOCKET:
                return "local:";
            case LOCAL_UNIX_SOCKET_ABSTRACT:
                return "localabstract:";
            case LOCAL_UNIX_SOCKET_FILE_SYSTEM:
                return "localfilesystem:";
            case LOCAL_UNIX_SOCKET_RESERVED:
                return "localreserved:";
            case REMOUNT:
                return "remount:";
            case REVERSE:
                return "reverse:";
            case SYNC:
                return "sync:";
            case TCP_CONNECT:
                return "tcp:";
            case TRACK_JDWP:
                return "track-jdwp";
            case BACKUP:
                return "backup:";
            case RESTORE:
                return "restore:";
            default:
                throw new IllegalArgumentException("Invalid service: " + service);
        }
    }

    @NonNull
    static String getDestination(@Services int service, @NonNull String... args) {
        String serviceName = getServiceName(service);
        StringBuilder destination = new StringBuilder(serviceName);
        switch (service) {
            case SHELL:
                for (String arg : args) {
                    if (arg.contains("\"")) {
                        throw new IllegalArgumentException("Arguments for inline shell cannot contain double" +
                                " quotations.");
                    }
                    if (arg.contains(" ")) {
                        destination.append("\"").append(Objects.requireNonNull(arg)).append("\"");
                    } else destination.append(Objects.requireNonNull(arg));
                }
                break;
            case FILE:
                if (args.length == 0) {
                    throw new IllegalArgumentException("File name must be specified.");
                } else if (args.length != 1) {
                    throw new IllegalArgumentException("Service expects exactly one argument, " + args.length
                            + " supplied.");
                }
                destination.append(Objects.requireNonNull(args[0]));
                break;
            case TCP_CONNECT:
                if (args.length == 0) {
                    throw new IllegalArgumentException("Port number must be specified.");
                } else if (args.length == 1) {
                    destination.append(args[0]);
                } else if (args.length == 2) {
                    destination.append(Objects.requireNonNull(args[0]))
                            .append(':')
                            .append(Objects.requireNonNull(args[1]));
                } else {
                    throw new IllegalArgumentException("Invalid number of arguments supplied.");
                }
                break;
            case LOCAL_UNIX_SOCKET:
            case LOCAL_UNIX_SOCKET_ABSTRACT:
            case LOCAL_UNIX_SOCKET_FILE_SYSTEM:
            case LOCAL_UNIX_SOCKET_RESERVED:
                if (args.length == 0) {
                    throw new IllegalArgumentException("Path must be specified.");
                } else if (args.length != 1) {
                    throw new IllegalArgumentException("Service expects exactly one argument, " + args.length
                            + " supplied.");
                }
                destination.append(Objects.requireNonNull(args[0]));
                break;
            case CONNECT_JDWP:
                if (args.length == 0) {
                    throw new IllegalArgumentException("PID must be specified.");
                } else if (args.length != 1) {
                    throw new IllegalArgumentException("Service expects exactly one argument, " + args.length
                            + " supplied.");
                }
                destination.append(Objects.requireNonNull(args[0]));
                break;
            case REVERSE:
                if (args.length == 0) {
                    throw new IllegalArgumentException("Forward command must be specified.");
                } else if (args.length != 1) {
                    throw new IllegalArgumentException("Service expects exactly one argument, " + args.length
                            + " supplied.");
                }
                if (args[0] == null) {
                    throw new IllegalArgumentException("Forward command is empty");
                }
                if ("list-forward".equals(args[0]) || "killforward-all".equals(args[0])) {
                    destination.append(args[0]);
                } else if (args[0].startsWith("forward:") || args[0].startsWith("killforward:")) {
                    destination.append(args[0]);
                } else {
                    throw new IllegalArgumentException("Invalid forward command.");
                }
                break;
            case BACKUP:
                if (args.length == 0) {
                    throw new IllegalArgumentException("At least one package must be specified or use -shared/-all.");
                }
            case REMOUNT:
                // Additional arguments for the commands
                destination.append(TextUtils.join(" ", args));
                break;
            case RESTORE:
            case FRAMEBUFFER:
            case SYNC:
            case TRACK_JDWP:
                if (args.length != 0) {
                    throw new IllegalArgumentException("Service expects no arguments.");
                }
                break;
        }
        return destination.toString();
    }
}
