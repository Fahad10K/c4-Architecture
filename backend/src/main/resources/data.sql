-- Seed data for Pizza Delivery Platform

-- Admin user (password: admin123)
INSERT INTO users (id, email, password_hash, name, phone, role, is_active, created_at, updated_at) VALUES
('u-admin-001', 'admin@pizzaplatform.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Platform Admin', '+1-555-0100', 'ADMIN', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Test customer (password: customer123)
INSERT INTO users (id, email, password_hash, name, phone, role, is_active, created_at, updated_at) VALUES
('u-cust-001', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John Doe', '+1-555-0101', 'CUSTOMER', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Test driver (password: driver123)
INSERT INTO users (id, email, password_hash, name, phone, role, is_active, created_at, updated_at) VALUES
('u-driver-001', 'driver@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mike Driver', '+1-555-0102', 'DRIVER', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Addresses
INSERT INTO addresses (id, user_id, label, street, city, state, zip_code, country, lat, lng, is_default) VALUES
('a-001', 'u-cust-001', 'Home', '123 Main St', 'New York', 'NY', '10001', 'US', 40.7128, -74.0060, true),
('a-002', 'u-cust-001', 'Office', '456 Broadway', 'New York', 'NY', '10002', 'US', 40.7208, -73.9990, false)
ON CONFLICT (id) DO NOTHING;

-- Stores
INSERT INTO stores (id, name, description, phone, email, street, city, state, zip_code, country, lat, lng, image_url, rating, review_count, is_active, open_time, close_time, delivery_radius, min_order_amount, delivery_fee, estimated_delivery_time, created_at, updated_at) VALUES
('s-001', 'Pizza Palace Downtown', 'Authentic Italian pizzas made with fresh ingredients and traditional recipes.', '+1-555-1001', 'downtown@pizzapalace.com', '789 Pizza Ave', 'New York', 'NY', '10003', 'US', 40.7300, -73.9950, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800', 4.5, 234, true, '10:00', '23:00', 15.0, 10.0, 3.99, 35, NOW(), NOW()),
('s-002', 'Pizza Palace Midtown', 'Quick-service pizza with gourmet toppings and craft beverages.', '+1-555-1002', 'midtown@pizzapalace.com', '321 Cheese Blvd', 'New York', 'NY', '10004', 'US', 40.7540, -73.9845, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800', 4.3, 189, true, '11:00', '22:00', 10.0, 12.0, 4.99, 40, NOW(), NOW()),
('s-003', 'Pizza Palace Brooklyn', 'Brooklyn-style pizza with a modern twist and locally sourced toppings.', '+1-555-1003', 'brooklyn@pizzapalace.com', '555 Flatbush Ave', 'Brooklyn', 'NY', '11225', 'US', 40.6602, -73.9690, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=800', 4.7, 312, true, '10:00', '00:00', 12.0, 8.0, 2.99, 30, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Categories
INSERT INTO categories (id, store_id, name, description, image_url, sort_order, is_active) VALUES
('c-001', 's-001', 'Classic Pizzas', 'Traditional favorites made with our signature dough', 'https://images.unsplash.com/photo-1604382355076-af4b0eb60143?w=400', 1, true),
('c-002', 's-001', 'Specialty Pizzas', 'Chef-crafted unique combinations', 'https://images.unsplash.com/photo-1571407970349-bc81e7e96d47?w=400', 2, true),
('c-003', 's-001', 'Sides & Appetizers', 'Perfect companions for your pizza', 'https://images.unsplash.com/photo-1541745537411-b8d1e8f9aef2?w=400', 3, true),
('c-004', 's-001', 'Beverages', 'Refreshing drinks', 'https://images.unsplash.com/photo-1527960471264-932f39eb5846?w=400', 4, true),
('c-005', 's-001', 'Desserts', 'Sweet endings', 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400', 5, true),
('c-006', 's-002', 'Classic Pizzas', 'Our best traditional pizzas', NULL, 1, true),
('c-007', 's-002', 'Gourmet Pizzas', 'Premium toppings and flavors', NULL, 2, true),
('c-008', 's-003', 'Brooklyn Originals', 'Our signature Brooklyn-style pies', NULL, 1, true),
('c-009', 's-003', 'Pasta & More', 'House-made pasta dishes', NULL, 2, true)
ON CONFLICT (id) DO NOTHING;

-- Menu Items for Store 1
INSERT INTO menu_items (id, store_id, category_id, name, description, price, image_url, is_available, is_popular, calories, preparation_time, customizations, tags) VALUES
('m-001', 's-001', 'c-001', 'Margherita', 'Fresh mozzarella, San Marzano tomato sauce, and fresh basil on our hand-tossed dough', 12.99, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400', true, true, 850, 15, '[{"name":"Size","options":["Small","Medium","Large"],"prices":[0,3,6]},{"name":"Crust","options":["Thin","Regular","Thick"],"prices":[0,0,1]},{"name":"Extra Cheese","options":["Yes","No"],"prices":[2,0]}]', 'vegetarian,classic'),
('m-002', 's-001', 'c-001', 'Pepperoni', 'Generous pepperoni with mozzarella and our signature tomato sauce', 14.99, 'https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400', true, true, 1050, 15, '[{"name":"Size","options":["Small","Medium","Large"],"prices":[0,3,6]},{"name":"Crust","options":["Thin","Regular","Thick"],"prices":[0,0,1]}]', 'classic,bestseller'),
('m-003', 's-001', 'c-001', 'Hawaiian', 'Ham, pineapple, mozzarella, and tomato sauce', 15.99, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400', true, false, 920, 15, '[{"name":"Size","options":["Small","Medium","Large"],"prices":[0,3,6]}]', 'classic'),
('m-004', 's-001', 'c-001', 'Four Cheese', 'Mozzarella, parmesan, gorgonzola, and ricotta', 16.99, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400', true, true, 980, 18, '[{"name":"Size","options":["Small","Medium","Large"],"prices":[0,3,6]}]', 'vegetarian,cheese'),
('m-005', 's-001', 'c-002', 'BBQ Chicken', 'Grilled chicken, BBQ sauce, red onions, and cilantro', 17.99, 'https://images.unsplash.com/photo-1571407970349-bc81e7e96d47?w=400', true, true, 1100, 18, '[{"name":"Size","options":["Small","Medium","Large"],"prices":[0,3,6]}]', 'specialty,chicken'),
('m-006', 's-001', 'c-002', 'Meat Lovers', 'Pepperoni, sausage, bacon, ham, and ground beef', 19.99, 'https://images.unsplash.com/photo-1604382355076-af4b0eb60143?w=400', true, true, 1350, 20, '[{"name":"Size","options":["Small","Medium","Large"],"prices":[0,3,6]}]', 'specialty,meat'),
('m-007', 's-001', 'c-002', 'Veggie Supreme', 'Bell peppers, mushrooms, onions, olives, tomatoes, and spinach', 16.99, 'https://images.unsplash.com/photo-1576458088443-04a19bb13da6?w=400', true, false, 780, 18, '[{"name":"Size","options":["Small","Medium","Large"],"prices":[0,3,6]}]', 'vegetarian,healthy'),
('m-008', 's-001', 'c-003', 'Garlic Breadsticks', 'Fresh-baked breadsticks with garlic butter and parmesan', 6.99, 'https://images.unsplash.com/photo-1541745537411-b8d1e8f9aef2?w=400', true, false, 420, 10, '[]', 'sides'),
('m-009', 's-001', 'c-003', 'Caesar Salad', 'Romaine lettuce, croutons, parmesan, and Caesar dressing', 8.99, 'https://images.unsplash.com/photo-1550304943-4f24f54ddde9?w=400', true, false, 350, 5, '[]', 'sides,healthy'),
('m-010', 's-001', 'c-003', 'Chicken Wings', 'Crispy wings with your choice of sauce: Buffalo, BBQ, or Garlic Parmesan', 10.99, 'https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400', true, true, 680, 15, '[{"name":"Sauce","options":["Buffalo","BBQ","Garlic Parmesan"],"prices":[0,0,0]}]', 'sides,chicken'),
('m-011', 's-001', 'c-004', 'Coca-Cola', 'Classic Coca-Cola', 2.99, 'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400', true, false, 140, 1, '[{"name":"Size","options":["Can","Bottle"],"prices":[0,1]}]', 'beverage'),
('m-012', 's-001', 'c-004', 'Craft Lemonade', 'House-made fresh lemonade', 3.99, NULL, true, false, 180, 2, '[]', 'beverage'),
('m-013', 's-001', 'c-005', 'Tiramisu', 'Classic Italian dessert with espresso-soaked ladyfingers', 7.99, 'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400', true, false, 450, 5, '[]', 'dessert'),
('m-014', 's-001', 'c-005', 'Chocolate Lava Cake', 'Warm chocolate cake with a molten center', 8.99, 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400', true, true, 520, 8, '[]', 'dessert')
ON CONFLICT (id) DO NOTHING;

-- Menu Items for Store 2
INSERT INTO menu_items (id, store_id, category_id, name, description, price, image_url, is_available, is_popular, calories, preparation_time, customizations, tags) VALUES
('m-015', 's-002', 'c-006', 'Margherita', 'Classic Margherita pizza', 13.99, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400', true, true, 850, 15, '[{"name":"Size","options":["Medium","Large"],"prices":[0,4]}]', 'classic'),
('m-016', 's-002', 'c-006', 'Pepperoni', 'Classic pepperoni pizza', 15.99, 'https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400', true, true, 1050, 15, '[{"name":"Size","options":["Medium","Large"],"prices":[0,4]}]', 'classic'),
('m-017', 's-002', 'c-007', 'Truffle Mushroom', 'Wild mushrooms, truffle oil, fontina, and fresh thyme', 21.99, NULL, true, true, 890, 20, '[{"name":"Size","options":["Medium","Large"],"prices":[0,4]}]', 'gourmet'),
('m-018', 's-002', 'c-007', 'Prosciutto & Arugula', 'Prosciutto di Parma, arugula, shaved parmesan, and balsamic glaze', 22.99, NULL, true, false, 950, 18, '[{"name":"Size","options":["Medium","Large"],"prices":[0,4]}]', 'gourmet')
ON CONFLICT (id) DO NOTHING;

-- Menu Items for Store 3
INSERT INTO menu_items (id, store_id, category_id, name, description, price, image_url, is_available, is_popular, calories, preparation_time, customizations, tags) VALUES
('m-019', 's-003', 'c-008', 'Brooklyn Classic', 'Extra-large thin crust with mozzarella and tomato sauce', 11.99, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400', true, true, 800, 12, '[{"name":"Size","options":["Slice","Whole Pie"],"prices":[0,8]}]', 'brooklyn,classic'),
('m-020', 's-003', 'c-008', 'The Williamsburg', 'Spicy soppressata, honey, and fresh basil', 14.99, NULL, true, true, 920, 15, '[{"name":"Size","options":["Slice","Whole Pie"],"prices":[0,10]}]', 'brooklyn,spicy'),
('m-021', 's-003', 'c-009', 'Baked Ziti', 'Classic baked ziti with ricotta and mozzarella', 13.99, NULL, true, false, 780, 20, '[]', 'pasta'),
('m-022', 's-003', 'c-009', 'Chicken Parmigiana', 'Breaded chicken cutlet with marinara and melted mozzarella', 15.99, NULL, true, false, 950, 22, '[]', 'entree')
ON CONFLICT (id) DO NOTHING;

-- Coupons
INSERT INTO coupons (id, code, description, discount_type, discount_value, min_order_amount, max_discount, usage_limit, used_count, is_active, expires_at, created_at, updated_at) VALUES
('cp-001', 'WELCOME20', 'Welcome offer - 20% off your first order', 'percentage', 20.0, 15.0, 10.0, 1000, 0, true, '2025-12-31 23:59:59', NOW(), NOW()),
('cp-002', 'FLAT5', '$5 off on orders above $25', 'fixed', 5.0, 25.0, 5.0, 500, 0, true, '2025-12-31 23:59:59', NOW(), NOW()),
('cp-003', 'FREEDELIVERY', 'Free delivery on orders above $20', 'fixed', 4.99, 20.0, 4.99, 200, 0, true, '2025-06-30 23:59:59', NOW(), NOW()),
('cp-004', 'SUMMER30', 'Summer special - 30% off', 'percentage', 30.0, 20.0, 15.0, 100, 0, true, '2025-09-30 23:59:59', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
