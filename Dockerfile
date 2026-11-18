FROM docker.io/library/eclipse-temurin:25-jre-alpine

COPY target/pronouns-standalone.jar /pronoun.is/app.jar

COPY resources/ /resources

ENV PORT 3000

EXPOSE 3000

CMD ["java", "-XX:-UseBASE64Intrinsics", "-jar", "/pronoun.is/app.jar" ]
