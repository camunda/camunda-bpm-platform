package org.camunda.spin.groovy.json.tree

node = S(input, "application/json")

property1 = node.prop("order");
property2 = node.prop("id");
property3 = node.prop("customers");
property4 = node.prop("orderDetails");
property5 = node.prop("active");

value1 = property1.isBoolean()
value2 = property2.isBoolean()
value3 = property3.isBoolean()
value4 = property4.isBoolean()
value5 = property5.isBoolean()