jsonNode = S(input, "application/json")

jsonNode.jsonPath('$.active').numberValue()