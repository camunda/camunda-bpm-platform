var jsonNode = JSON(input);

node = jsonNode.jsonPath('$.customers[0]').element();