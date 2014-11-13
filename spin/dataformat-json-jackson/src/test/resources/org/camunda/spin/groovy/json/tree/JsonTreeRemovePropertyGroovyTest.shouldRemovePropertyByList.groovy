package org.camunda.spin.groovy.json.tree

node = S(input, "application/json")
def list = ["order", "active"]
node.deleteProp(list)

value1 = node.hasProp("order")
value2 = node.hasProp("active")