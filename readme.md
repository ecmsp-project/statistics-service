How to send kafka messages:
1. Run docker compose up
2. Run dev profile
3. Go to http://localhost:8088 - it's kafka dashboard form here you can send messages, topics should already be created as it's provided in properties files


Example usage for variant-sold event:
```json
{
    "eventId": "550e8400-e29b-41d4-a716-446655440001",
    "variantId": "660e8400-e29b-41d4-a716-446655440002",
    "productId": "770e8400-e29b-41d4-a716-446655440003",
    "productName": "Premium Wireless Headphones",
    "soldAt": 199.99,
    "quantitySold": 3,
    "margin": 45.50,
    "stockRemaining": 47
  }
```

Example for variant-stock-updated event:
```json
{
    "eventId": "880e8400-e29b-41d4-a716-446655440004",
    "variantId": "660e8400-e29b-41d4-a716-446655440002",
    "deliveredQuantity": 100,
    "deliveredAt": "2025-11-15T10:30:00"
}
```