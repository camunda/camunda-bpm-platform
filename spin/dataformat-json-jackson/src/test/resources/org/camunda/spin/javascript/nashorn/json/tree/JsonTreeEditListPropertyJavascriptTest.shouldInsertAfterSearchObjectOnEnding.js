var node = S(input, "application/json");
var currencies = node.prop("orderDetails").prop("currencies");

oldSize = currencies.elements().size();
oldValue = currencies.elements().get(1).stringValue();

currencies.insertAfter(oldValue, "Test");

newSize = currencies.elements().size();
newValue = currencies.elements().get(2).stringValue();
oldValueOnNewPosition = currencies.elements().get(1).stringValue();
