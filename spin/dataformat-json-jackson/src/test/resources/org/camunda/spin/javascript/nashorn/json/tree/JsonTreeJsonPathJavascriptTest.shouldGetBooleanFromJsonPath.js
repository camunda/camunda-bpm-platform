var jsonNode = S(input, "application/json");

booleanValue = jsonNode.jsonPath('$.active').boolValue();