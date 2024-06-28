jsonNode = S($input, "application/json")

$numberValue = jsonNode.jsonPath('$.id').numberValue()