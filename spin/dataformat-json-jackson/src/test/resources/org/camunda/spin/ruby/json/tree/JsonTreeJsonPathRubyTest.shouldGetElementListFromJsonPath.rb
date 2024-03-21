jsonNode = S($input, "application/json")

$nodeList = jsonNode.jsonPath('$.customers').elementList()