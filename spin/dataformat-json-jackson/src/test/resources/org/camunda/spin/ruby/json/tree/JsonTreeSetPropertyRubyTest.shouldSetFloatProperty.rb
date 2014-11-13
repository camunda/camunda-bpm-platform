node = S($input, "application/json")
node.prop("comment", 42.00)

$propertyNode = node.prop("comment")
$value = $propertyNode.numberValue()