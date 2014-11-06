package org.camunda.spin.groovy.json.tree

jsonNode = JSON(input);

nodeList = jsonNode.jsonPath('$.customers[0:2]').elementList();