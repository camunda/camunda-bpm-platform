node = JSON($input)

$oldValue = node.prop("order")

json_object = {
    "name" => "test",
    "comment" => "42!"
}

node.prop("order", json_object)
$newValue = node.prop("order")