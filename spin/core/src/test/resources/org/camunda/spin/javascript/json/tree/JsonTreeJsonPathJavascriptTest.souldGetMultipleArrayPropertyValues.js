var jsonNode = JSON(input);

nodeList = jsonNode.jsonPath('$.customers[*].name').elementList();