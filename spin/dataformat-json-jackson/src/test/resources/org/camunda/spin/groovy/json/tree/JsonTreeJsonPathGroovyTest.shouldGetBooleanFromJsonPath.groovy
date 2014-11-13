package org.camunda.spin.groovy.json.tree

jsonNode = S(input, "application/json");

booleanValue = jsonNode.jsonPath('$.active').boolValue();