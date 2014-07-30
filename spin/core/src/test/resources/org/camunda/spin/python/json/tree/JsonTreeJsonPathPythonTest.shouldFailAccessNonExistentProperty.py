jsonNode = JSON(input)

jsonNode.jsonPath('$.order.task').elementList()