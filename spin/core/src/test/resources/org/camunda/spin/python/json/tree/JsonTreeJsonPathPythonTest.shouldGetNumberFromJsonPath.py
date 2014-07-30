jsonNode = JSON(input)

numberValue = jsonNode.jsonPath('$.id').number()