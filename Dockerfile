FROM openjdk:11-slim-buster

# copy default CodeNarc ruleset
ADD codenarc.ruleset /

# TODO Temporary. Remove it when local testing is done
ADD code/ /

# copy built distribution files to image
ADD target/distribution /var/pfxprobe

# add pfxprobe bin to executable path
ENV PATH=$PATH:/var/pfxprobe/bin

# TODO - tepomrary - switch when local testing is done
ENTRYPOINT "/bin/sh"
#CMD [ "pfxprobe" ]