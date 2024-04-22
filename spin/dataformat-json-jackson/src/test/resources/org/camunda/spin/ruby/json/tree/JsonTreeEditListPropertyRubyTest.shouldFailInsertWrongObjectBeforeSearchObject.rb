require 'date'

node = S($input, "application/json")
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertBefore("euro", Date.today)
