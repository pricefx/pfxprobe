FROM openjdk:11-slim-buster

# copy built distribution files to image
ADD target/distribution /var/pfxprobe

# add pfxprobe bin to executable path
ENV PATH=$PATH:/var/pfxprobe/bin

CMD [ "pfxprobe" ]