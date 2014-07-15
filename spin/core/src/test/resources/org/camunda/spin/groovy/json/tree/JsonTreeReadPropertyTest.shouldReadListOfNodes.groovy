package org.camunda.spin.groovy.json.tree

node = JSON(input)
list = node.fieldNames()

value1 = list.get(0)
value2 = list.get(1)
value3 = list.get(4)