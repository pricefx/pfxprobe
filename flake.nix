{
  description = "pfxprobe - Groovy code quality scanner";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    { nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        pp = pkgs.writeShellScriptBin "pp" ''
          set -e
          SCRIPT_DIR="$(pwd)"

          if [ ! -f "$SCRIPT_DIR/target/pfxprobe-1.0.jar" ]; then
            echo "Error: pfxprobe-1.0.jar not found. Run 'mvn package' first."
            exit 1
          fi

          ${pkgs.temurin-bin-21}/bin/java -jar "$SCRIPT_DIR/target/pfxprobe-1.0.jar" "$@"
        '';

        pp-test = pkgs.writeShellScriptBin "pp-test" ''
          set -e
          ${pkgs.maven}/bin/mvn test
        '';

        pp-test-coverage = pkgs.writeShellScriptBin "pp-test-coverage" ''
          set -e
          ${pkgs.maven}/bin/mvn clean test
        '';

      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            maven
            temurin-bin-21
            pp
            pp-test
            pp-test-coverage
          ];
        };
      }
    );
}
