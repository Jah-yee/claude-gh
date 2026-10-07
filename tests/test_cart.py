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


def test_cart_no_shared_items():
    # Cart() should not share items with other Cart()
    c1 = Cart()
    c1.add(Item("apple", 1.0))
    assert len(Cart().items) == 0
    # Cart([]) should not share with Cart([])
    shared = []
    c2 = Cart(shared)
    c3 = Cart([])
    c2.add(Item("banana", 2.0))
    assert len(c3.items) == 0
    assert len(Cart().items) == 0
