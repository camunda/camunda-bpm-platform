package org.camunda.spin.groovy.json.tree

node = S(input, "application/json")
node.deleteProp("order")
value = node.hasProp("order")