require 'date'

node = JSON($input)
customers = node.prop("customers")

customers.appendAt(1, Date.today)