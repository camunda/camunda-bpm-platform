package org.camunda.spin.groovy.json.tree

jsonNode = JSON(input);

node = jsonNode.jsonPath('$.customers[0]').element();