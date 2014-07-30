jsonNode = JSON($input)

$nodeList = jsonNode.jsonPath('$.customers').elementList()