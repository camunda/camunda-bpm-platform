package org.camunda.spin.groovy.json.tree

node = S(input, "application/json")
childNode = node.prop("orderDetails")

property = childNode.prop("article")

value = property.stringValue()
