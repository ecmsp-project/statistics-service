#!/usr/bin/env python3
"""
Generate realistic sales data for statistics-service init.sql
6 months of sales data for 12 product variants with different patterns
"""

import uuid
from datetime import datetime, timedelta
import random

# Product configurations
PRODUCTS = [
    {
        'id': 1, 'variant_id': 'aaaaaaaa-1111-1111-1111-111111111111',
        'product_id': 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'name': 'Premium Laptop 15inch', 'price': 1299.99, 'margin': 250.00,
        'pattern': 'bestseller', 'daily_avg': 8, 'variance': 3
    },
    {
        'id': 2, 'variant_id': 'bbbbbbbb-2222-2222-2222-222222222222',
        'product_id': 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'name': 'Gaming Mouse RGB', 'price': 79.99, 'margin': 25.00,
        'pattern': 'bestseller', 'daily_avg': 15, 'variance': 5
    },
    {
        'id': 3, 'variant_id': 'cccccccc-3333-3333-3333-333333333333',
        'product_id': 'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'name': 'Wireless Keyboard', 'price': 89.99, 'margin': 30.00,
        'pattern': 'steady', 'daily_avg': 5, 'variance': 2
    },
    {
        'id': 4, 'variant_id': 'dddddddd-4444-4444-4444-444444444444',
        'product_id': 'dddddddd-dddd-dddd-dddd-dddddddddddd',
        'name': 'USB-C Hub', 'price': 49.99, 'margin': 15.00,
        'pattern': 'trending_up', 'daily_avg': 4, 'variance': 2
    },
    {
        'id': 5, 'variant_id': 'eeeeeeee-5555-5555-5555-555555555555',
        'product_id': 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'name': 'Monitor 27inch 4K', 'price': 599.99, 'margin': 120.00,
        'pattern': 'slow_moving', 'daily_avg': 1, 'variance': 1
    },
    {
        'id': 6, 'variant_id': 'ffffffff-6666-6666-6666-666666666666',
        'product_id': 'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'name': 'Webcam 1080p', 'price': 119.99, 'margin': 40.00,
        'pattern': 'depleting', 'daily_avg': 3, 'variance': 1
    },
    {
        'id': 7, 'variant_id': 'aaaaaaaa-7777-7777-7777-777777777777',
        'product_id': 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'name': 'Headphones Wireless', 'price': 159.99, 'margin': 50.00,
        'pattern': 'seasonal', 'daily_avg': 6, 'variance': 3
    },
    {
        'id': 8, 'variant_id': 'bbbbbbbb-8888-8888-8888-888888888888',
        'product_id': 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'name': 'External SSD 1TB', 'price': 149.99, 'margin': 35.00,
        'pattern': 'overstocked', 'daily_avg': 2, 'variance': 1
    },
    {
        'id': 9, 'variant_id': 'cccccccc-9999-9999-9999-999999999999',
        'product_id': 'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'name': 'Phone Case Premium', 'price': 29.99, 'margin': 12.00,
        'pattern': 'high_turnover', 'daily_avg': 20, 'variance': 8
    },
    {
        'id': 10, 'variant_id': 'dddddddd-0000-0000-0000-000000000000',
        'product_id': 'dddddddd-dddd-dddd-dddd-dddddddddddd',
        'name': 'Charging Cable 2m', 'price': 19.99, 'margin': 8.00,
        'pattern': 'steady', 'daily_avg': 12, 'variance': 4
    },
    {
        'id': 11, 'variant_id': 'eeeeeeee-1111-1111-1111-111111111111',
        'product_id': 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'name': 'Desk Mat XL', 'price': 39.99, 'margin': 15.00,
        'pattern': 'trending_down', 'daily_avg': 5, 'variance': 2
    },
    {
        'id': 12, 'variant_id': 'ffffffff-2222-2222-2222-222222222222',
        'product_id': 'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'name': 'LED Strip 5m', 'price': 34.99, 'margin': 10.00,
        'pattern': 'new_product', 'daily_avg': 8, 'variance': 3
    },
]

# Delivery dates for stock tracking
DELIVERIES = {
    1: ['2024-05-18', '2024-06-10', '2024-07-02', '2024-07-25', '2024-08-16', '2024-09-08', '2024-10-01', '2024-10-24'],
    2: ['2024-05-20', '2024-06-15', '2024-07-10', '2024-08-05', '2024-08-30', '2024-09-25', '2024-10-20'],
    3: ['2024-05-22', '2024-07-01', '2024-08-15', '2024-10-01'],
    4: ['2024-05-25', '2024-07-05', '2024-08-20', '2024-10-10'],
    5: ['2024-06-01', '2024-09-15'],
    6: ['2024-05-28', '2024-07-15'],
    7: ['2024-05-30', '2024-07-20', '2024-09-20', '2024-10-25'],
    8: ['2024-06-05', '2024-10-15'],
    9: ['2024-05-19', '2024-06-12', '2024-07-08', '2024-08-02', '2024-08-26', '2024-09-20', '2024-10-15'],
    10: ['2024-05-21', '2024-07-12', '2024-09-05'],
    11: ['2024-05-24', '2024-07-18', '2024-09-28'],
    12: ['2024-09-15', '2024-10-28'],
}

INITIAL_STOCK = {
    1: 150, 2: 300, 3: 200, 4: 100, 5: 80, 6: 120,
    7: 150, 8: 500, 9: 400, 10: 500, 11: 200, 12: 180
}

start_date = datetime(2024, 5, 18)
end_date = datetime(2024, 11, 18)

def get_daily_multiplier(date, pattern):
    """Get sales multiplier based on day of week and pattern"""
    weekday = date.weekday()

    # Weekend boost (Friday-Sunday)
    weekend_boost = 1.3 if weekday >= 4 else 1.0

    # Pattern-specific adjustments
    if pattern == 'seasonal':
        # Higher sales in Q4 (October-November)
        month = date.month
        seasonal_boost = 1.5 if month >= 10 else 1.0
        return weekend_boost * seasonal_boost

    return weekend_boost

def get_trending_multiplier(date, pattern, start_date):
    """Get multiplier for trending products"""
    days_elapsed = (date - start_date).days
    total_days = 184  # ~6 months

    if pattern == 'trending_up':
        # Sales increase 3x over 6 months
        return 1.0 + (2.0 * days_elapsed / total_days)
    elif pattern == 'trending_down':
        # Sales decrease to 33% over 6 months
        return 1.0 - (0.67 * days_elapsed / total_days)

    return 1.0

def calculate_stock_after_delivery(product_id, date, deliveries_dict):
    """Calculate stock level after most recent delivery before this date"""
    delivery_dates = [datetime.strptime(d, '%Y-%m-%d') for d in deliveries_dict.get(product_id, [])]
    delivery_amounts = {
        1: [150, 120, 140, 160, 180, 200, 150, 170],
        2: [300, 250, 280, 320, 300, 350, 280],
        3: [200, 180, 200, 220],
        4: [100, 150, 200, 250],
        5: [80, 60],
        6: [120, 80],
        7: [150, 120, 250, 300],
        8: [500, 400],
        9: [400, 350, 380, 420, 400, 450, 380],
        10: [500, 450, 480],
        11: [200, 150, 100],
        12: [180, 220],
    }

    # Find most recent delivery
    recent_deliveries = [d for d in delivery_dates if d <= date]
    if not recent_deliveries:
        return INITIAL_STOCK.get(product_id, 100)

    last_delivery_idx = delivery_dates.index(max(recent_deliveries))
    return delivery_amounts[product_id][last_delivery_idx]

print("-- ============================================")
print("-- SALES DATA (SOLD TABLE)")
print("-- ============================================")
print("-- Generated realistic sales patterns for 6 months")
print()

current_stock = INITIAL_STOCK.copy()
current_date = start_date

while current_date <= end_date:
    for product in PRODUCTS:
        pid = product['id']
        pattern = product['pattern']

        # Skip new product before launch
        if pattern == 'new_product' and current_date < datetime(2024, 9, 15):
            continue

        # Skip depleting product after stock runs out
        if pattern == 'depleting' and current_date > datetime(2024, 10, 15):
            continue

        # Calculate sales for the day
        base_qty = product['daily_avg']
        variance = product['variance']

        daily_mult = get_daily_multiplier(current_date, pattern)
        trend_mult = get_trending_multiplier(current_date, pattern, start_date)

        # Random variation
        random_factor = random.uniform(0.7, 1.3)

        quantity = max(0, int(base_qty * daily_mult * trend_mult * random_factor))

        # Reduce to 0-2 sales on some days for variety
        if random.random() < 0.15:  # 15% chance of very slow day
            quantity = random.randint(0, 2)

        if quantity == 0:
            continue

        # Check for delivery on this day
        delivery_date_str = current_date.strftime('%Y-%m-%d')
        if delivery_date_str in DELIVERIES.get(pid, []):
            current_stock[pid] = calculate_stock_after_delivery(pid, current_date, DELIVERIES)

        # Limit by available stock
        if current_stock.get(pid, 0) < quantity:
            quantity = max(0, current_stock.get(pid, 0))

        if quantity > 0:
            current_stock[pid] = current_stock.get(pid, 0) - quantity

            # Generate sale record
            sale_time = current_date + timedelta(hours=random.randint(9, 20),
                                                  minutes=random.randint(0, 59))

            total_price = round(product['price'] * quantity, 2)

            print(f"INSERT INTO SOLD VALUES (")
            print(f"    '{uuid.uuid4()}'::uuid,")
            print(f"    '{product['variant_id']}'::uuid,")
            print(f"    '{product['product_id']}'::uuid,")
            print(f"    '{product['name']}',")
            print(f"    {product['price']},")
            print(f"    {quantity},")
            print(f"    {product['margin']},")
            print(f"    {current_stock[pid]},")
            print(f"    '{sale_time.strftime('%Y-%m-%d %H:%M:%S')}'")
            print(f");")

    current_date += timedelta(days=1)

print()
print("-- Sales data generation complete")
print(f"-- Period: {start_date.strftime('%Y-%m-%d')} to {end_date.strftime('%Y-%m-%d')}")
