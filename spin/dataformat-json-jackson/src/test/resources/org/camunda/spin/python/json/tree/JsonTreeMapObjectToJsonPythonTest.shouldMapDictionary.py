def customer(name, date):
  return {"name": name, "contractStartDate": date}

order = {"order" : "order1", "dueUntil": 20150112, "id": 1234567890987654321, "active": True, "nullValue": None}

customers = [customer("Kermit", 1354539722), customer("Waldo", 1320325322), customer("Johnny", 1286110922)]
order["customers"] = customers

orderDetails = {"article": "camundaBPM", "price": 32000.45, "roundedPrice": 32000, "currencies": ["euro", "dollar"], "paid": False}
order["orderDetails"] = orderDetails

json = S(order, "application/json").toString()
