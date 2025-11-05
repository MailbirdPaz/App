# 1️⃣ Base image with Java 23
FROM openjdk:23-jdk-slim

# 2️⃣ Set work directory
WORKDIR /app

# 3️⃣ Install curl and unzip to download JavaFX SDK
RUN apt-get update && apt-get install -y curl unzip && rm -rf /var/lib/apt/lists/*

# 4️⃣ Download and extract JavaFX SDK (Linux version)
RUN curl -L -o javafx-sdk.zip https://download2.gluonhq.com/openjfx/23/openjfx-23_linux-x64_bin-sdk.zip \
    && unzip javafx-sdk.zip -d /opt \
    && mv /opt/javafx-sdk-23 /opt/javafx \
    && rm javafx-sdk.zip

# 5️⃣ Copy compiled classes (from your host)
COPY target/classes /app/classes

# 6️⃣ Copy Maven dependencies directly from your local .m2 repository
# (You can change this path if needed — make sure to build with `-f` argument from the correct dir)
COPY /Users/mac/.m2/repository /root/.m2/repository

# 7️⃣ Run the app with JavaFX modules and all dependencies
CMD ["java", \
     "--module-path", "/opt/javafx/lib", \
     "--add-modules", "javafx.controls,javafx.fxml", \
     "-Dfile.encoding=UTF-8", \
     "-cp", "classes:/root/.m2/repository/*", \
     "org.mailbird.Starter"]
