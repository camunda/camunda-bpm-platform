node = S(input, "application/json")
customers = node.prop("customers")

customers.remove(None)