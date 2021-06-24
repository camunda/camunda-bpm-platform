var node = S(input, "application/json");
var currencies = node.prop("orderDetails").prop("currencies");

currencies.insertAt(-6, "test1");
