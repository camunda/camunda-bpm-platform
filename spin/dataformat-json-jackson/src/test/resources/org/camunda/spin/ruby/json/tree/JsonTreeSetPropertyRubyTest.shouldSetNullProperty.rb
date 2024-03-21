node = S($input, "application/json")
node.prop("comment", nil)

$propertyNode = node.prop("comment")
$newValue = node.prop("comment").value()