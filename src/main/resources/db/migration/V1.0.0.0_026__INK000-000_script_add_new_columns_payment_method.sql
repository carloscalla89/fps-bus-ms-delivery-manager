ALTER TABLE `payment_method`
ADD COLUMN card_provider_id int(8) NULL AFTER payment_type
ADD COLUMN card_provider_code VARCHAR(8) NULL AFTER card_provider_id,
ADD COLUMN bin VARCHAR(8) NULL AFTER card_name,
ADD COLUMN coupon VARCHAR(64) NULL;