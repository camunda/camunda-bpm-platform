package org.camunda.spin.groovy.json.tree

node = S(input, "application/json")
childNode1 = node.prop("orderDetails")

property1 = node.prop("dueUntil")
property2 = node.prop("id")
property3 = childNode1.prop("price")

value1 = property1.numberValue()
value2 = property2.numberValue()
value3 = property3.numberValue()
