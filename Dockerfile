FROM docker.io/library/eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /src/everest
COPY . .
RUN ./gradlew clean bootjar

FROM docker.io/library/eclipse-temurin:21-jre-jammy AS runner

ARG USER_NAME=everest_usr
ARG USER_UID=1000
ARG USER_GID=${USER_UID}

RUN addgroup -g ${USER_GID} ${USER_NAME} \
    && adduser -h /opt/everest -D -u ${USER_UID} -G ${USER_NAME} ${USER_NAME} \

# Install dependencies for gRPC
RUN apt-get update && \
    apt-get install -y --no-install-recommends libstdc++6 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* \

USER ${USER_NAME}
WORKDIR /opt/everest
COPY --from=builder --chown=${USER_UID}:${USER_GID} /src/everest/build/libs/*.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java"]
CMD ["-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar", "--server.port=3000"]