# NullGate integration

This patch adds `NULLGATE` as a ColorBlendr work method. ColorBlendr sends a
typed system-theme request to the separately installed NullGate controller and
does not receive a root shell, a privileged binder, or arbitrary command
execution.

## Trust and request flow

1. ColorBlendr verifies the installed NullGate APK against its pinned SHA-256
   signing-certificate digest.
2. A theme change is accepted only while ColorBlendr is in the foreground.
3. ColorBlendr launches NullGate explicitly for a result with exactly four
   fields: protocol version, opaque ARGB seed, supported Monet style, and a
   60-second duration.
4. NullGate independently verifies ColorBlendr's package, UID ownership,
   version, and official signing certificate before showing user approval.
5. ColorBlendr stores only the returned lease ID and expiry receipt. It revokes
   an existing lease before requesting a replacement.
6. Removing ColorBlendr's fabricated colors requests immediate revocation.

The current NullGate policy reviews ColorBlendr version code 42 and the official
ColorBlendr signer. A future upstream release must be reviewed and its new
version code admitted by NullGate before the release can use the integration.

## Deliberate limitations

- Background, boot, and Tasker-triggered theme changes fail closed in NullGate
  mode because every privileged request requires a visible, result-bound user
  confirmation.
- `CMF` is not part of the NullGate theme capability and is rejected.
- The first compatibility duration is fixed at 60 seconds for reversible
  validation. Extending the lease is a separate policy decision.
- A locally debug-signed ColorBlendr APK cannot impersonate the official app and
  will be rejected by NullGate.

## Host verification

```text
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

Do not uninstall the official ColorBlendr or install a debug-signed replacement
on a daily-driver device merely to test this patch.
