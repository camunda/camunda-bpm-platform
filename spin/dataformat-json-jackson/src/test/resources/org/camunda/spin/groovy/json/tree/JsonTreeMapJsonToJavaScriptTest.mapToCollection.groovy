import com.fasterxml.jackson.databind.type.TypeFactory

desiredType = TypeFactory.defaultInstance().constructCollectionType(collectionType, mapToType)

result = JSON(input).mapTo(desiredType.toCanonical())

