package org.camunda.spin.groovy.json.tree

jsonNode = JSON(input);

nodeList = jsonNode.jsonPath('$.customers[*].name').elementList();