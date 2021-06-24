node = S(input, "application/json");
node["prop(java.lang.String,java.lang.String)"]("order", null);

propertyNode = node.prop("order");
newValue = node.prop("order").value();