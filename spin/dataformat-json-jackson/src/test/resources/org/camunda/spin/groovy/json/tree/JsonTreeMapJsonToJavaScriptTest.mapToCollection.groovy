import com.fasterxml.jackson.databind.type.TypeFactory

desiredType = TypeFactory.defaultInstance().constructCollectionType(collectionType, mapToType)

result = S(input, "application/json").mapTo(desiredType.toCanonical())

