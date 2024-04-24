node = S(input, "application/json")

oldValue = node.prop("order")

jsonObject = {
    "name": "test",
    "comment": "42!"
}

node.prop("order", jsonObject)
newValue = node.prop("order")