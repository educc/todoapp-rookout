FROM openjdk:8-jdk as release

#PASS THIS TOKEN AT BUILD TIME IS NOT A BEST PRACTICE, YOU SHOULD CHANGE THIS
ARG ROOKOUT_TOKEN

RUN mkdir -p /app
# Copy the jar image (which already include resoures)
COPY build/libs/tutorial-1.0.0.jar  /app/tutorial-1.0.0.jar

# Copy the rook.jar downloaded in build phase
COPY rook.jar /app/rook.jar


ENV JAVA_TOOL_OPTIONS="-javaagent:/app/rook.jar -DROOKOUT_TOKEN=$ROOKOUT_TOKEN -DROOKOUT_LABELS=env:dev"
#ENTRYPOINT ["java", "-javaagent:rook.jar", "-jar", "/app/tutorial-1.0.0.jar"]

ENTRYPOINT ["java", "-jar", "/app/tutorial-1.0.0.jar"]
