desiredType = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructCollectionType($collectionType, $mapToType)

$result = JSON($input).mapTo(desiredType.toCanonical())

