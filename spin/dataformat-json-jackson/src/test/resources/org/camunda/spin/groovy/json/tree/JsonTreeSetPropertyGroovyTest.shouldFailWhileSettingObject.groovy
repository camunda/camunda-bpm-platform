package org.camunda.spin.groovy.json.tree

node = S(input, "application/json");
Date date = new Date();
def object = [
    date: date
];

node.prop("comment", object);