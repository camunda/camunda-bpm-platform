var node = S(input, "application/json");
var currencies = node.prop("orderDetails").prop("currencies");

currencies.insertBefore(null, "test");