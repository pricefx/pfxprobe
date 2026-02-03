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
              JAR_PATH="$(pwd)/build/result/repo/${pname}-${pversion}.jar"

              if [ ! -f "$JAR_PATH" ]; then
                echo "Jar not found, packaging first"
                ${aliases.nxpackage-jar.script}
              fi

              ${pkgs.temurin-bin-21}/bin/java -jar "$JAR_PATH" "$@"
            '';
          };

          nxpackage-test = {
            description = "Test built JAR and Docker distributions";
            script = ''
              set -e
              docker load -i build/result-docker
              ./scripts/test-packages.sh
            '';
          };

          nxpackage-jar = {
            description = "Build JAR distribution via nix";
            script = ''
              set -e
              mkdir -p build
              nix build .#default --out-link build/result
              echo "JAR built at: build/result/repo/${pname}-${pversion}.jar"
            '';
          };

          nxpackage-docker = {
            description = "Build Docker archive via nix";
            script = ''
              set -e
              mkdir -p build
              nix build .#docker --out-link build/result-docker-link
              # Copy the actual file (not symlink) for GitLab artifacts
              cp build/result-docker-link build/result-docker
              echo "Docker archive built at: build/result-docker"
            '';
          };

          nxpackage = {
            description = "Build all packages (JAR and Docker) and test execute them";
            script = ''
              set -e
              ${aliases.nxpackage-jar.script}
              ${aliases.nxpackage-docker.script}
              ${aliases.nxpackage-test.script}
            '';
          };

          nxtest = {
            description = "Run unit tests";
            script = ''
              set -e
              # Run tests without clean to avoid conflicts with nix build outputs
              mvn test --batch-mode
            '';
          };

          nxpublish = {
            description = "Publish to Maven repository";
            script = ''
              set -e
              mvn deploy -s src/main/assembly/ci_settings.xml
            '';
          };

          nxci-push = {
            description = "Push Docker image to registry (CI)";
            script = ''
              set -e
              if [ -z "''${CI_COMMIT_REF_NAME:-}" ]; then
                echo "Error: This script is only for CI environments (CI_COMMIT_REF_NAME not set)"
                exit 1
              fi

              # Create policy.json for skopeo if not present
              mkdir -p /etc/containers
              echo '{"default":[{"type":"insecureAcceptAnything"}]}' > /etc/containers/policy.json

              APP_VERSION=$(grep -oP '(?<=<version>)[^<]+' pom.xml | head -1)
              BRANCH_TAG=''${CI_COMMIT_REF_NAME:-latest}
              BRANCH_TAG=''${BRANCH_TAG/master/latest}

              echo "Pushing Docker image with tags - version=$APP_VERSION branch=$BRANCH_TAG"

              if [ "''${CI_COMMIT_REF_NAME:-}" = "master" ]; then
                echo "Pushing version tag - ''${DOCKER_REGISTRY}:$APP_VERSION"
                skopeo copy docker-archive:build/result-docker \
                  docker://''${DOCKER_REGISTRY}:$APP_VERSION \
                  --dest-creds ''${DOCKER_USER}:''${DOCKER_PASS}
              fi

              echo "Pushing branch tag - ''${DOCKER_REGISTRY}:$BRANCH_TAG"
              skopeo copy docker-archive:build/result-docker \
                docker://''${DOCKER_REGISTRY}:$BRANCH_TAG \
                --dest-creds ''${DOCKER_USER}:''${DOCKER_PASS}
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
            mvn clean package -Dmaven.repo.local=$TMPDIR/.m2/repository
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
        # NOTE: We install to /opt/pfxprobe to avoid overwriting /bin from base image
        # (which contains /bin/sh needed by GitLab Runner)
        packages.docker = pkgs.dockerTools.buildImage {
          name = pname;
          tag = pversion;
          fromImage = pkgs.dockerTools.pullImage {
            imageName = "eclipse-temurin";
            imageDigest = "sha256:cc11c035bb25cc709d7b1e3f43cbff15d69e06e2dba23eec431b64627d27e705";
            hash =
              {
                aarch64-darwin = "sha256-vIQE6HDXEvAYi6yeQFxT+gpo+k2hQRvie3GplDdB2KY=";
                x86_64-linux = "sha256-k0XrQYpp33yKL9bUg2hEh8Ng5vca4BlhPsryBLiRcSk=";
                aarch64-linux = ""; # add once known/needed
                x86_64-darwin = ""; # add once known/needed
              }
              .${system};
          };
          copyToRoot = pkgs.buildEnv {
            name = "image-root";
            paths = [
              # Install pfxprobe to /opt/pfxprobe instead of root to avoid shadowing /bin
              (pkgs.runCommand "pfxprobe-opt" { } ''
                mkdir -p $out/opt/pfxprobe
                cp -r ${self.packages.${system}.default}/* $out/opt/pfxprobe/
              '')
              (pkgs.runCommand "rulesets" { } ''
                mkdir -p $out
                cp ${./codenarc.ruleset} $out/codenarc.ruleset
                cp ${./codenarc_accelerator.ruleset} $out/codenarc_accelerator.ruleset
              '')
            ];
          };
          config = {
            WorkingDir = "/";
            Env = [
              "PATH=/opt/pfxprobe/bin:/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
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
            docker
            util-linux
            skopeo
          ];
        };
      }
    );
}
