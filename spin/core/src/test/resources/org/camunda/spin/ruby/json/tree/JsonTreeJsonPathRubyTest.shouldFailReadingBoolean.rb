jsonNode = JSON($input)

jsonNode.jsonPath('$.order').boolValue()