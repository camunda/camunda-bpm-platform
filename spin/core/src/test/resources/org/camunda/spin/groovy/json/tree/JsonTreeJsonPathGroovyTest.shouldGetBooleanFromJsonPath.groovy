package org.camunda.spin.groovy.json.tree

jsonNode = JSON(input);

booleanValue = jsonNode.jsonPath('$.active').bool();