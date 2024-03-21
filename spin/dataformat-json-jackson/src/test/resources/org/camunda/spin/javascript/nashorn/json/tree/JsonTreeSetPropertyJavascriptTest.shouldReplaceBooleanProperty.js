node = S(input, "application/json");

oldValue = node.prop("order").stringValue();

node.prop("order", false);
newValue = node.prop("order").boolValue();