jsonNode = S($input, "application/json")

$numberValue = jsonNode.jsonPath('$.orderDetails.price').numberValue()