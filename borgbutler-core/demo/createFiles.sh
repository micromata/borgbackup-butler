#!/bin/bash

export BORG_PASSPHRASE='borgbutler123'
export BORG_COMMAND='/root/bin/borg-linux64'
export TEST_DIR='/root/borgbutler-demo'

if [ -f $BORG_COMMAND ]; then
  echo Borg command already exists...
else
  echo Downloading borg;
  mkdir /root/bin
  cd /root/bin
  curl -LJO https://github.com/borgbackup/borg/releases/download/1.1.8/borg-linux64
  chmod 700 $BORG_COMMAND
fi;

echo Creating backup dir /backup-test...
rm -rf /backup-test
mkdir /backup-test

echo Initializing borg backup...
$BORG_COMMAND init --encryption=repokey /backup-test

function backup() {
echo Creating backup...
$BORG_COMMAND create --filter AME                    \
                     --stats                         \
                     --progress                      \
                     --show-rc                       \
                     --compression lz4               \
                     --exclude-caches                \
                     /backup-test::borgbutlerdemo-$1 \
                     /home /root /etc /usr/bin /usr/sbin /opt
}

rm -rf $TEST_DIR
mkdir $TEST_DIR
cd $TEST_DIR
touch README.txt
chmod 700 README.txt
echo `ls /usr` > filelist
touch oldfile

backup 2019-01-12_01-00

rm oldfile
mkdir newDir
touch newDir/newfile
chown borgbutler.users README.txt
chmod 755 README.txt
echo `ls /` >> filelist

backup 2019-01-13_01-00

cd /root
rm -rf out
mkdir out
cd out
$BORG_COMMAND info --json /backup-test >repo-info.json
$BORG_COMMAND list --json /backup-test >repo-list.json

$BORG_COMMAND info --json /backup-test::borgbutlerdemo-2019-01-12_01-00 >archive-info-borgbuterldemo-2019-01-12_01-00.json
$BORG_COMMAND info --json /backup-test::borgbutlerdemo-2019-01-13_01-00 >archive-info-borgbuterldemo-2019-01-13_01-00.json

$BORG_COMMAND list --json-lines /backup-test::borgbutlerdemo-2019-01-12_01-00 >archive-list-borgbuterldemo-2019-01-12_01-00.json
$BORG_COMMAND list --json-lines /backup-test::borgbutlerdemo-2019-01-13_01-00 >archive-list-borgbuterldemo-2019-01-13_01-00.json

gzip -9 *
cd /root
tar cvf out.tar out
