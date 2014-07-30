jsonNode = JSON($input)

$stringValue = jsonNode.jsonPath('$.order').string()