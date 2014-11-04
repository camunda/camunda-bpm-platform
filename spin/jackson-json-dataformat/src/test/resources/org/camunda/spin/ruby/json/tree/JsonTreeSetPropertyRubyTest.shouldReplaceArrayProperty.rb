node = JSON($input)

$oldValue = node.prop("order")

list = ["test", "test2"]

node.prop("order", list)
$newValue = node.prop("order")