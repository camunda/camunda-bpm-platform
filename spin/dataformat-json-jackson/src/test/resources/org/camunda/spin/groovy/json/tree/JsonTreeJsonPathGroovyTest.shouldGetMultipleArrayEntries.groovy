package org.camunda.spin.groovy.json.tree

jsonNode = S(input, "application/json");

nodeList = jsonNode.jsonPath('$.customers[0:2]').elementList();