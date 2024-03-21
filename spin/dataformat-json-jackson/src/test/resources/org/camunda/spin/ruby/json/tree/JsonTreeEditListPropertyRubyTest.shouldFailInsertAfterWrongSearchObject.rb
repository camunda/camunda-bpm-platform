require 'date'

node = S($input, "application/json")
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertAfter(Date.today, "test")