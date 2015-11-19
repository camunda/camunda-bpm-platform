node = S(input, "application/json");
node["prop(String, String)"]("comment", null);

propertyNode = node.prop("comment");
newValue = node.prop("comment").value();