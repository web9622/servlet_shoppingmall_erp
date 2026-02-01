# Build stage
FROM gradle:8.4-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Run stage
FROM tomcat:10.1-jdk17-temurin
COPY --from=build /app/build/libs/Shoppingmall_Company_ERP-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
