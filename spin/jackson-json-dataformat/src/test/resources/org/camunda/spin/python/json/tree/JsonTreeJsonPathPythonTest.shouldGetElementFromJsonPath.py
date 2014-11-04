jsonNode = JSON(input)

node = jsonNode.jsonPath('$.orderDetails').element()