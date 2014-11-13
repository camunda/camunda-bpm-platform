node = S(input, "application/json")

oldValue = node.prop("order")

list = [
    "test",
    "test2"
]

node.prop("order", list)
newValue = node.prop("order")