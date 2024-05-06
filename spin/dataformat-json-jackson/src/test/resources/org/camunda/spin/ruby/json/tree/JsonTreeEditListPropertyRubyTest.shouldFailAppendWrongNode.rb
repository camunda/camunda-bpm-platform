require 'date'

node = S($input, "application/json")
customers = node.prop("customers")

customers.append(Date.today)