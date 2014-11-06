package org.camunda.spin.groovy.json.tree

node = JSON(input);
customers = node.prop("customers");

customers.remove(null);