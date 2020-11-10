# This is a local development testing script for windows execution of narc using docker

docker rm -f "narctest"
docker build -t "pfxnarc" .
docker run --name "narctest" --rm -v ${PWD}/src:/code pfxnarc