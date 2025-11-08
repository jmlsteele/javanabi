# Agent Guidelines for Javanabi

## Build/Test Commands
- Build: `mvn compile`
- Test: `mvn test`
- Single test: `mvn test -Dtest=ClassName#methodName`
- Integration test: `mvn verify`
- Lint: `mvn checkstyle:check`
- Format: `mvn spotless:apply`
- Package: `mvn package`
- Run server: `mvn exec:java -Dexec.mainClass="com.javanabi.HanabiServer"`

## Code Style Guidelines
- Use Java 17+ with strict typing
- Follow Java naming conventions (camelCase for variables/methods, PascalCase for classes, UPPER_SNAKE_CASE for constants)
- Import organization: java.* first, then javax.*, then org.*, then com.* (alphabetical within each group)
- Error handling: use checked exceptions for recoverable errors, unchecked for programming errors
- Method naming: descriptive verbs, avoid abbreviations, use boolean prefixes (is, has, can)
- Class naming: nouns or noun phrases, avoid generic names
- Max line length: 120 characters
- Use meaningful variable names, avoid single letters except for loop counters (i, j, k)

## Project Structure
- Maven standard layout: src/main/java, src/test/java, src/main/resources
- Package structure: com.javanabi.{domain,server,game,client,util}
- Keep tests in matching package structure under src/test/java
- Use dependency injection (Spring/Guice) for service layer
- Separate game logic from networking code