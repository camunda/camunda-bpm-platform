package org.camunda.spin.groovy.json.tree

node = S(input, "application/json");
customers = node.prop("customers");

customers.remove(new Date());
