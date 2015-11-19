node = S($input, "application/json")
node.prop("order", nil)

$propertyNode = node.prop("order")
$newValue = node.prop("order").value()