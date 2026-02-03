{
  description = "pfxprobe - Groovy code quality scanner";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
      ...
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        # Read and parse pom.xml for artifactId and version
        pomContent = builtins.readFile ./pom.xml;
        pname = getPomProperty "artifactId" pomContent;
        pversion = getPomProperty "version" pomContent;

        # Helper scripts for dev / CI shells
        aliases = {
          nxhelp = {
            description = "Print project info and available commands";
            script = ''
              echo "Shortcuts Available:"
              printf '%s\t%s\t%s\n' \
                ${
                  builtins.concatStringsSep " " (
                    builtins.map (name: "\"- ${name}\" \":\" \"${aliases.${name}.description}\"") (
                      builtins.attrNames aliases
                    )
                  )
                } \
                | column -t -s $'\t'
            '';
          };

          nxdev = {
            description = "Build & run the ${pname} java binary application";
            script = ''
              set -e
              JAR_NAME="${pname}-${pversion}.jar"
              JAR_PATH="$(pwd)/target/$JAR_NAME"

              if [ ! -f "$JAR_PATH" ]; then
                echo "Jar not found, packaging first"
                ${aliases.nxjar.script}
              fi

              ${pkgs.temurin-bin-21}/bin/java -jar "$JAR_PATH" "$@"
            '';
          };

          nxjar = {
            description = "Build maven/jar package locally (runs tests)";
            script = ''
              set -e
              mvn clean package
            '';
          };

          nxdist = {
            description = "Build nix packages and run distribution tests";
            script = ''
              set -e
              echo "Building default package..."
              nix build .#default --out-link result-default
              echo "Building docker package..."
              nix build .#docker --out-link result-docker
              echo ""
              ${aliases.nxdist-test.script}
            '';
          };

          nxdist-test = {
            description = "Test built JAR and Docker distributions";
            script = ''
              set -e
              ./scripts/test-distributions.sh
            '';
          };
        };

        pkgs = nixpkgs.legacyPackages.${system};
        lib = pkgs.lib;

        # Derive the above aliases to binary scripts to install in devshell
        aliasBins = builtins.attrValues (
          builtins.mapAttrs (name: value: pkgs.writeShellScriptBin name value.script) aliases
        );

        # Helper function to get a property value from pom.xml <properties> area
        getPomProperty =
          tag: xmlContent:
          let
            afterOpening = lib.head (lib.tail (lib.splitString ("<" + tag + ">") xmlContent));
            value = lib.head (lib.splitString ("</" + tag + ">") afterOpening);
          in
          value;

        # Build the Maven project
        pfxprobe = pkgs.stdenv.mkDerivation {
          name = "${pname}-${pversion}";
          src = lib.cleanSourceWith {
            src = ./.;
            filter =
              path: type:
              lib.hasSuffix ".groovy" path
              || lib.hasSuffix "pom.xml" path
              || lib.hasSuffix ".ruleset" path
              || type == "directory";
          };
          buildInputs = [
            pkgs.maven
            pkgs.temurin-bin-21
          ];
          buildPhase = ''
            export HOME=$TMPDIR
            mvn clean package -DskipTests -Dmaven.repo.local=$TMPDIR/.m2/repository
          '';
          installPhase = ''
            mkdir -p $out
            cp -r target/distribution/* $out/
            # Fix shebang to use /usr/bin/sh (available in eclipse-temurin base image)
            sed -i "1s|.*|#!/usr/bin/sh|" $out/bin/${pname}
          '';
          doCheck = false;
          dontPatchShebangs = true;
        };
      in
      {
        # Default package - the Maven distribution
        packages.default = pfxprobe;

        # Docker image using eclipse-temurin base (matching original Dockerfile)
        packages.docker = pkgs.dockerTools.buildImage {
          name = pname;
          tag = pversion;
          fromImage = pkgs.dockerTools.pullImage {
            imageName = "eclipse-temurin";
            imageDigest = "sha256:cc11c035bb25cc709d7b1e3f43cbff15d69e06e2dba23eec431b64627d27e705";
            hash = "sha256-vIQE6HDXEvAYi6yeQFxT+gpo+k2hQRvie3GplDdB2KY=";
          };
          copyToRoot = pkgs.buildEnv {
            name = "image-root";
            paths = [
              self.packages.${system}.default
              (pkgs.runCommand "rulesets" { } ''
                mkdir -p $out
                cp ${./codenarc.ruleset} $out/
                cp ${./codenarc_accelerator.ruleset} $out/
              '')
            ];
          };
          config = {
            Cmd = [ "${self.packages.${system}.default}/bin/pfxprobe" ];
            Env = [
              "PATH=${
                self.packages.${system}.default
              }/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
            ];
          };
        };

        # Main development shell for project dev with dependencies added & a print hook to display helpers
        devShells.default = pkgs.mkShell {

          # Default executed commend on shell-enter
          shellHook = ''
            echo "Welcome to the ${pname} v${pversion} dev-shell."
            nxhelp
          '';

          # What dependencies to include in the dev shell
          buildInputs = with pkgs; [
            aliasBins
            maven
            temurin-bin-21
          ];
        };
      }
    );
}
