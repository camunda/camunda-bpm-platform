var node = JSON(input);
var currencies = node.prop("orderDetails").prop("currencies");

value = currencies.indexOf("dollar");