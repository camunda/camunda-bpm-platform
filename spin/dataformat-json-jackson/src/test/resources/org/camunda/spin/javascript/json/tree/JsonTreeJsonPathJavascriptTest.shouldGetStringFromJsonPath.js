var jsonNode = S(input, "application/json");

stringValue = jsonNode.jsonPath('$.order').stringValue();