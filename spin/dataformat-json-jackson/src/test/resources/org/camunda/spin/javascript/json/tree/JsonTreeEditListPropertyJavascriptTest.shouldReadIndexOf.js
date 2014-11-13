var node = S(input, "application/json");
var currencies = node.prop("orderDetails").prop("currencies");

value = currencies.indexOf("dollar");