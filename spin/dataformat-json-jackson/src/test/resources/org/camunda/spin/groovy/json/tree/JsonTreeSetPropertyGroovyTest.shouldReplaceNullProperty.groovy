node = S(input, "application/json");
node.prop("order", (String) null);

propertyNode = node.prop("order");
newValue = node.prop("order").value();