#!/bin/bash

echo "Make sure, that you've run 'gradle dist' on top directory first"

echo "Unpacking distribution zip to target/dependency..."
rm -rf app/target/dependency
mkdir -p app/target/dependency && (cd app/target/dependency; unzip ../../../../borgbutler-server/build/distributions/borgbutler-server-*.zip)

# Will be done by application after starting: (cd app; wget https://github.com/borgbackup/borg/releases/download/1.1.16/borg-linux64.tgz)

echo "Building docker file..."
(cd app; docker build -t kreinhard/borgbutler .)

echo "Push: docker push kreinhard/borgbutler:tagname"
echo "Run without ssh: 'docker run -v $HOME/BorgButler:/BorgButler -p 127.0.0.1:9042:9042 --name borgbuttler kreinhard/borgbutler'"
echo "Run with ssh: 'docker run -v $HOME/BorgButler:/BorgButler -v $HOME/.ssh:/home/borgbutler/.ssh:ro -p 127.0.0.1:9042:9042 --name borgbuttler kreinhard/borgbutler'"
