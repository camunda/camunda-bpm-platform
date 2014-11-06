jsonNode = JSON(input)

stringValue = jsonNode.jsonPath('$.order').stringValue()