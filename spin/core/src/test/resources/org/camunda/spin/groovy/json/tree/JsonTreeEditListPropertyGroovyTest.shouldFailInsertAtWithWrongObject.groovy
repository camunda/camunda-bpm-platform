package org.camunda.spin.groovy.json.tree

node = JSON(input);
customers = node.prop("customers");
customers.insertAt(1, new Date());
