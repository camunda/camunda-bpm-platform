package org.camunda.spin.groovy.json.tree

node = JSON(input);
customers = node.prop("customers");

customers.removeLast(null);