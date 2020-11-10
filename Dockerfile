FROM groovy:jre11

ADD src/ /var/pfxnarc

VOLUME /code
WORKDIR /code

ENTRYPOINT [ "groovy", "/var/pfxnarc/pfxnarc.groovy" ]