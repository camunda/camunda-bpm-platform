var node = S(input, "application/json");
var currencies = node.prop("orderDetails").prop("currencies");

oldSize = currencies.elements().size();
oldValue = currencies.elements().get(0).stringValue();

currencies.remove("euro");

newValue = currencies.elements().get(0).stringValue();
newSize = currencies.elements().size();
