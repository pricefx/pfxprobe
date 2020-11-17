FROM openjdk:11-slim-buster

ADD target/distribution /var/pfxnarc

ENV PATH=$PATH:/var/pfxnarc/bin

CMD [ "pfxnarc" ]