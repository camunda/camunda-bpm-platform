jsonNode = JSON($input)

$booleanValue = jsonNode.jsonPath('$.active').bool()