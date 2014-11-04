package org.camunda.spin.groovy.json.tree

node = JSON(input);
Date date = new Date();
def object = [
    date: date
];

node.prop("comment", object);