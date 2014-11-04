import com.fasterxml.jackson.databind.type.TypeFactory as TypeFactory

desiredType = TypeFactory.defaultInstance().constructCollectionType(collectionType, mapToType)

result = JSON(input).mapTo(desiredType.toCanonical())

