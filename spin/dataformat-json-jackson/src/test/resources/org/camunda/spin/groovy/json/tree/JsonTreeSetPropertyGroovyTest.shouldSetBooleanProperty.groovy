node = S(input, "application/json");
node.prop("comment", false);

propertyNode = node.prop("comment");
value = propertyNode.boolValue();