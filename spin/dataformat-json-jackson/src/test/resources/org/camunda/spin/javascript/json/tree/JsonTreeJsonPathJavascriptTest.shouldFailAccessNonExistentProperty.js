var jsonNode = S(input, "application/json");

jsonNode.jsonPath('$.order.task').elementList();