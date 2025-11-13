FROM eclipse-temurin:21-jre-jammy

# copy default CodeNarc ruleset
ADD codenarc.ruleset /

# copy Accelerator CodeNarc ruleset
ADD codenarc_accelerator.ruleset /

# copy built distribution files to image
ADD target/distribution /var/pfxprobe

# add pfxprobe bin to executable path
ENV PATH=$PATH:/var/pfxprobe/bin

CMD [ "pfxprobe" ]