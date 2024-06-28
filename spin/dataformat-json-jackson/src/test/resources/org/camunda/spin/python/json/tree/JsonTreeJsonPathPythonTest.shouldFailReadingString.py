jsonNode = S(input, "application/json")

jsonNode.jsonPath('$.id').stringValue()