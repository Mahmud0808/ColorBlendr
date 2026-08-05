{
  description = "ColorBlendr development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    android-nixpkgs = {
      url = "github:tadfisher/android-nixpkgs";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, android-nixpkgs }:
    let
      supportedSystems = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];
      forAllSystems = nixpkgs.lib.genAttrs supportedSystems;
      nixpkgsFor = forAllSystems (system: import nixpkgs { inherit system; config.allowUnfree = true; });
    in
    {
      devShells = forAllSystems (system:
        let
          pkgs = nixpkgsFor.${system};
          sdk = android-nixpkgs.sdk.${system} (sdkPkgs: with sdkPkgs; [
            cmdline-tools-latest
            build-tools-34-0-0
            build-tools-35-0-0
            build-tools-36-0-0
            build-tools-37-0-0
            platform-tools
            platforms-android-34
            platforms-android-35
            platforms-android-36
            # platforms-android-37-0 # Add if needed once stable
          ]);
        in
        {
          default = pkgs.mkShell {
            buildInputs = with pkgs; [
              jdk21
              sdk
              gradle
              android-tools
            ];

            shellHook = ''
              export JAVA_HOME=${pkgs.jdk21.home}
              export ANDROID_HOME=${sdk}/share/android-sdk
              export ANDROID_SDK_ROOT=$ANDROID_HOME
              # Platform tools for adb
              export PATH=$ANDROID_HOME/platform-tools:$PATH

              echo "ColorBlendr Development Environment"
              echo "JDK: $(java -version 2>&1 | head -n 1)"
              echo "Android SDK: $ANDROID_HOME"
            '';
          };
        });
    };
}
