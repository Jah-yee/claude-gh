from shopcart import Cart, Item, parse_price


def test_add_and_total():
    cart = Cart()
    cart.add(Item("apple", 1.50, 2))
    cart.add(Item("bread", 3.00))
    assert cart.total() == 6.00


def test_add_merges_same_item():
    cart = Cart([])
    cart.add(Item("apple", 1.50))
    cart.add(Item("apple", 1.50, 3))
    assert len(cart.items) == 1
    assert cart.items[0].quantity == 4


def test_parse_simple_price():
    assert parse_price("$12.50") == 12.50
