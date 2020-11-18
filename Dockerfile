FROM openjdk:11-slim-buster

# copy built distribution files to image
ADD target/distribution /var/pfxnarc

# add pfxnarc bin to executable path
ENV PATH=$PATH:/var/pfxnarc/bin

ENTRYPOINT [ "pfxnarc" ]