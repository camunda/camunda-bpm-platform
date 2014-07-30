package org.camunda.spin.groovy.json.tree

jsonNode = JSON(input);

stringValue = jsonNode.jsonPath('$.order').string();