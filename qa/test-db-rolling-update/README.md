# Rolling Update Test Suite

How this test suite works (`$CURRENT` refers to current minor version, `$PREVIOUS` refers to `$CURRENT - 1`):

1. Create DB schema for `$PREVIOUS`
1. Execute test setup with `$PREVIOUS` engine
1. Patch DB schema to `$CURRENT`
1. Execute test setup with `$CURRENT` engine
1. Run tests with `$PREVIOUS` engine

## Executing Tests

Run `mvn clean install -Prolling-update,${DATABASE}`.
