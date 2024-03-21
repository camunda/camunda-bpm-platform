node = S(input, "application/json");
node.prop("comment", (String) null);

propertyNode = node.prop("comment");
value = propertyNode.value();