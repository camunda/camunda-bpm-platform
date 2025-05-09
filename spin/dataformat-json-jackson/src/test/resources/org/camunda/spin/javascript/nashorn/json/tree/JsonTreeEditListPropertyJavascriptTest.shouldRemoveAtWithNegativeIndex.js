var node = S(input, "application/json");
var currencies = node.prop("orderDetails").prop("currencies");

oldSize = currencies.elements().size();

currencies.removeAt(-2);

newSize = currencies.elements().size();
value = currencies.elements().get(newSize - 1).stringValue();
