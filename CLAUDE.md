# Development Guidelines

## Project Overview

**dailyprnt** assembles a daily strip of personal content from independent modules, in the
spirit of Berg's Little Printer. Long-term target is a thermal printer; for now it is
delivered as a web page shaped like the printed strip.

**Read [docs/requirements.md](docs/requirements.md) before making design decisions** — it
defines the module concept, strip geometry, and what is deliberately out of scope.

Service built with **Quarkus** and **Java 21**

## Code Style & Formatting

- **Validation**: Formatting is validated during the Maven `validate` phase
- Always ensure code is properly formatted before committing
- Avoid using the 'var' type

## Testing Guidelines

### Test Structure

All tests MUST follow the **Given-When-Then** pattern with explicit comment markers:

```java
@Test
void shouldDoSomething()
{
    // given
    String input = "test data";

    // when
    String result = service.process(input);

    // then
    assertEquals("expected", result);
}
```

### Testing Philosophy

- **Minimize mocking**: Mock as little as needed in tests. Prefer using the real implementations whenever possible
- **Use `@QuarkusTest`**: Leverage Quarkus's test framework for integration testing
- **Database cleanup**: Use `@AfterEach` with `@Transactional` to clean up test data (see `ShortUrlResourceTest` for examples)
- **Test real scenarios**: Focus on end-to-end testing using RestAssured rather than unit testing with heavy mocking

### Test Execution

- **Always run tests** after making changes: `./mvnw test`
- Tests are run automatically during the build process
- Use `@Inject` for dependency injection in tests when accessing repositories or services

### Example Test Pattern

```java
@QuarkusTest
class ExampleTest
{
    @Inject
    ExampleRepository repository;

    @AfterEach
    @Transactional
    void cleanup()
    {
        repository.deleteAll();
    }

    @Test
    void shouldDoFeature1()
    {
        // given
        String testData = "input";

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(testData)
            .when()
            .post("/endpoint")
            .then()
            .statusCode(200);
    }
}
```

## Architecture & Technology Stack

### Core Technologies

- **Framework**: Quarkus 3.28.4
- **Language**: Java 21
- **Database**: PostgreSQL with Hibernate ORM Panache
- **Validation**: `quarkus-hibernate-validator` for HTTP request and response validation
- **REST**: Quarkus REST with Jackson
- **Testing**: JUnit 5 + RestAssured

## Development Workflow

### Running the Application

```bash
# Development mode with live reload
./mvnw quarkus:dev

# Run tests
./mvnw test

# Build package
./mvnw package

### Making Changes

1. Make code changes following the project's code style
3. Write tests following the Given-When-Then pattern
4. Run `./mvnw test` to verify all tests pass
5. Commit changes with descriptive commit messages

## Documentation Guidelines

### README.md

- **Do NOT include project structure** in the README
- Focus on setup, configuration, and usage instructions
- Keep examples practical and up-to-date
- Include both API and UI usage examples

### Code Documentation

- Keep comments concise, meaningful, short and limited
- Prefer self-documenting code over excessive comments

## Configuration

## Commit Message Convention

Use official GitMoji commit message convention, e.g.
- 📝 `(docs):` - Documentation changes
- 🎨 `(design):` - UI/UX changes
- 👷 `(ci):` - CI/CD changes
- ✨ `(feat):` - New features
- 🐛 `(fix):` - Bug fixes
- ♻️ `(refactor):` - Code refactoring

## Best Practices

1. **Dependency Injection**: Use `@Inject` for dependency injection, avoid manual instantiation
2. **Panache Pattern**: Use Panache's Repository pattern for database operations, do not use Active Record. Extend from PanacheEntity to have the correct Id, sequence etc.
3. **Transaction Management**: Use `@Transactional` for methods that modify data
4. **Error Handling**: Return appropriate HTTP status codes with meaningful error messages by throwing WebApplicationException or a subclass
5. **HTTP Validation**: Apply Bean Validation annotations and `@Valid` via `quarkus-hibernate-validator` in resource layers to enforce input constraints
6. **Security**: Always secure admin endpoints with authentication
7. **Resource Cleanup**: Use try-with-resources for streams and connections

## Common Tasks

### Modifying the Database Schema

1. Update the entity class (`ShortUrl.java` or `User.java`)
2. Quarkus will auto-update the schema in dev mode
3. Update affected tests
4. Consider migration strategy for production

### Adding New Features

1. Plan the feature and identify affected components
2. Write tests first (TDD approach encouraged)
3. Implement the feature
4. Update documentation
5. Run all tests before committing
