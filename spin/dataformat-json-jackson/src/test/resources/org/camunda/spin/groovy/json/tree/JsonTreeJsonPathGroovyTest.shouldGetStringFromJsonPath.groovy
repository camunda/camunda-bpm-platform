package org.camunda.spin.groovy.json.tree

jsonNode = S(input, "application/json");

stringValue = jsonNode.jsonPath('$.order').stringValue();