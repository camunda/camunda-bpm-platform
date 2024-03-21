jsonNode = S(input, "application/json")

node = jsonNode.jsonPath('$.orderDetails').element()