FROM openjdk:16-jdk-buster

RUN addgroup borgbutler && adduser --ingroup borgbutler borgbutler
# ProjectForge's base dir: must be mounted on host file system:
RUN mkdir /BorgButler
# Grant access for user projectforge:
RUN chown borgbutler.borgbutler /BorgButler
VOLUME /BorgButler

RUN mkdir /home/borgbutler/.ssh
RUN chown borgbutler.borgbutler /home/borgbutler/.ssh
VOLUME /home/borgbutler/.ssh

EXPOSE 9042

USER borgbutler:borgbutler

# Don't put fat jar files in docker images: https://phauer.com/2019/no-fat-jar-in-docker-image/
ARG DEPENDENCY=target/dependency/borgbutler-server-0.5-SNAPSHOT
COPY ${DEPENDENCY}/lib /app/lib
COPY ${DEPENDENCY}/web /app/web
#COPY ${DEPENDENCY}/META-INF /app/META-INF
#COPY ${DEPENDENCY}/BOOT-INF/classes /app

COPY --chown=borgbutler:borgbutler entrypoint.sh /app
RUN chmod 755 /app/entrypoint.sh
#COPY shutdown.sh /app
#COPY startup.sh /app

#ARG JAVA_OPTS="-Xms1g -Xmx1g"
#ENV JAVA_OPTS_VAR=$JAVA_OPTS

# -Dloader.path=${HOME}/ProjectForge/resources/milton ${DEBUGOPTS}

# Variable expansion doesn't work for ENTRYPOINT definition as array, but array is required, because graceful shutdown of
# container isn't given if java is started via 'sh -c' as it will be done by ENTRYPOINT java .....
# Java options are modifiable by user through own ENTRYPOINT definition on docker run or in docker-compose.yml.
ENTRYPOINT ["/app/entrypoint.sh"]

MAINTAINER Micromata
