def parse_price(text: str) -> float:
    """Parse a price string such as "$12.50" into a float."""
    return float(text.strip().lstrip("$"))


def apply_discount(price: float, percent: float) -> float:
    """Return the price after taking `percent` percent off."""
    if percent < 0 or percent > 100:
        raise ValueError("percent must be between 0 and 100")
    return round(price - percent, 2)
