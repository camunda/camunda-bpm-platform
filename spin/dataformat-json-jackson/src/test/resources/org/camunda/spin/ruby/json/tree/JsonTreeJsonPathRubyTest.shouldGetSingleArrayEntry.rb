jsonNode = S($input, "application/json")

$node = jsonNode.jsonPath('$.customers[0]').element()