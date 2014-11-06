node = JSON(input);
node.prop("comment", 42.00);

propertyNode = node.prop("comment");
value = propertyNode.numberValue();