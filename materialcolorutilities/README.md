# materialcolorutilities

Sources come from [material-color-utilities](https://github.com/material-foundation/material-color-utilities),
tracked as a git submodule in `upstream/`. Nothing in `upstream/` is edited.

At build time the `generate<Variant>McuSources` task copies `upstream/kotlin/**/*.kt` into
`build/generated/kotlin/` and rewrites the upstream packages (`dynamiccolor`, `hct`, `palettes`, ...)
to `com.drdisagree.materialcolorutilities.*`.

## Init

After a fresh clone:

```
git submodule update --init
```

Or clone with submodules in one step:

```
git clone --recurse-submodules https://github.com/Mahmud0808/ColorBlendr
```

The build fails with a missing `upstream/kotlin` input directory if the submodule isn't initialized.

## Update

Pull the latest upstream revision, rebuild, then commit the new submodule pointer:

```
git submodule update --remote materialcolorutilities/upstream
./gradlew :app:assembleDebug
git add materialcolorutilities/upstream
git commit -m "Update material color utilities"
```

To pin a specific upstream commit instead:

```
cd materialcolorutilities/upstream
git checkout <commit>
cd ../..
git add materialcolorutilities/upstream
```

Upstream changes can alter generated colors, so check the app after updating.
