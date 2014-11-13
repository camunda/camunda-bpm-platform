node = S($input, "application/json")
node.prop("comment", 4200000000)

$propertyNode = node.prop("comment")
$value = $propertyNode.numberValue()