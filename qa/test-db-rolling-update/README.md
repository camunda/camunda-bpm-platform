# Rolling Update Test Suite

How this test suite works (`$CURRENT` refers to current minor version, `$PREVIOUS` refers to `$CURRENT - 1`):

1. Create DB schema for `$PREVIOUS`
1. Execute test setup with `$PREVIOUS` engine
1. Patch DB schema to `$CURRENT`
1. Execute test setup with `$CURRENT` engine
1. Run tests with `$PREVIOUS` engine

## Executing Tests

Run `mvn clean install -Prolling-update,${DATABASE}`.

### Running tests with the Maven Wrapper

With `mvnw`, from the root of the project,
run: `./mvnw clean install -f qa/test-db-rolling-update/pom.xml -Prolling-update,${database-id}`
where `${database-id}` is for example `h2`.