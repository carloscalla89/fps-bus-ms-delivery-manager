ALTER TABLE order_fulfillment ADD COLUMN pay_order_date VARCHAR(200) NULL;
ALTER TABLE order_fulfillment ADD COLUMN transaction_order_date  VARCHAR(200) NULL;
ALTER TABLE order_fulfillment ADD COLUMN purchase_number  VARCHAR(200) NULL;
ALTER TABLE order_fulfillment ADD COLUMN scheduled_order_date  VARCHAR(200) NULL;
ALTER TABLE order_fulfillment ADD COLUMN pos_code VARCHAR(200) NULL;
