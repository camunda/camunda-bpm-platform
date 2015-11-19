node = S(input, "application/json");
node["prop(String, String)"]("order", null);

propertyNode = node.prop("order");
newValue = node.prop("order").value();