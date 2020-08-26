FROM openjdk:8-jdk as release

ARG ROOKOUT_TOKEN

RUN mkdir -p /app
# Copy the jar image (which already include resoures)
COPY build/libs/tutorial-1.0.0.jar  /app/tutorial-1.0.0.jar

# Copy the rook.jar downloaded in build phase
COPY rook.jar /app/rook.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/app/rook.jar -DROOKOUT_TOKEN=$TOKEN -DROOKOUT_LABELS=env:dev"
#ENTRYPOINT ["java", "-javaagent:rook.jar", "-jar", "/app/tutorial-1.0.0.jar"]

ENTRYPOINT ["java", "-jar", "/app/tutorial-1.0.0.jar"]
