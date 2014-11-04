package org.camunda.spin.groovy.json.tree

node = JSON(input)
def list = ["order", "active"]
node.deleteProp(list)

value1 = node.hasProp("order")
value2 = node.hasProp("active")