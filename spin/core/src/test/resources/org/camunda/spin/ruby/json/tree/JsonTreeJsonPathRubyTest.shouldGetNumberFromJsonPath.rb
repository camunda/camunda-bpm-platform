jsonNode = JSON($input)

$numberValue = jsonNode.jsonPath('$.id').numberValue()