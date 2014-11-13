node = S(input, "application/json")
list = ["order", "active"]
node.deleteProp(list)

value1 = node.hasProp("order")
value2 = node.hasProp("active")