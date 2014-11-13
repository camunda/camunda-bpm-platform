node = S($input, "application/json")
currencies = node.prop("orderDetails").prop("currencies")

$oldSize = currencies.elements().size()

currencies.append("Testcustomer")

$newSize = currencies.elements().size()
$value = currencies.elements().get($oldSize).stringValue()
