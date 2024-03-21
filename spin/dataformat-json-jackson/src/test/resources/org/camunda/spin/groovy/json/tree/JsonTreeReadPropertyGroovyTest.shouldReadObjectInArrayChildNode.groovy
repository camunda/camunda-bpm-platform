package org.camunda.spin.groovy.json.tree

node = S(input, "application/json")
childNode = node.prop("customers")
list = childNode.elements()

property1 = list.get(0)
property2 = list.get(1)

customer1 = property1.prop("name")
customer2 = property2.prop("name")

value1 = customer1.stringValue();
value2 = customer2.stringValue();

