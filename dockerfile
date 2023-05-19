FROM maven:3.9.2-eclipse-temurin-20-alpine
RUN mkdir ./songday && mkdir /root/.m2
COPY . /songday
WORKDIR /songday
ENV TZ=America/Fortaleza
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN mvn clean package -DskipTests
RUN mv target/*.jar target/app.jar
ENTRYPOINT ["java","-jar","target/app.jar", \
"--spring.data.mongodb.uri=mongodb://${MONGODB_USERNAME}:${MONGODB_PASSWORD}@${MONGODB_NAME}:${MONGODB_PORT}", \
"--spring.data.mongodb.database=${MONGODB_DATABASE}"]