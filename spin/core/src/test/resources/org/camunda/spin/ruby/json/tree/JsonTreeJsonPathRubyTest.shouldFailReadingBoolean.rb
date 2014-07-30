jsonNode = JSON($input)

jsonNode.jsonPath('$.order').bool()