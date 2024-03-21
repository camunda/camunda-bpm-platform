desiredType = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructCollectionType($collectionType, $mapToType)

$result = S($input, "application/json").mapTo(desiredType.toCanonical())

