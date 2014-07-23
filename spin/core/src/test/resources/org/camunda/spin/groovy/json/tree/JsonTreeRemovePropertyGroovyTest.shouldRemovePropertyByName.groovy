package org.camunda.spin.groovy.json.tree

node = JSON(input)
node.deleteProp("order")
value = node.hasProp("order")