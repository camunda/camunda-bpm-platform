package org.camunda.spin.groovy.json.tree

node = S(input, "application/json");
customers = node.prop("customers");
customers.append(new Date());