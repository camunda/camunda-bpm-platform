node = S(input, "application/json");
node["prop(java.lang.String,java.lang.String)"]("comment", null);

propertyNode = node.prop("comment");
newValue = node.prop("comment").value();