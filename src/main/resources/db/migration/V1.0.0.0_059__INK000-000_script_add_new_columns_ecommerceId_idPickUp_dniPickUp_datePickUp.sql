ALTER TABLE `order_fulfillment`
    ADD COLUMN id_pick_up TINYINT(1) NULL DEFAULT NULL AFTER voucher;

ALTER TABLE `order_fulfillment`
    ADD COLUMN dni_pick_up VARCHAR(10) NULL DEFAULT NULL AFTER id_pick_up;

ALTER TABLE `order_fulfillment`
    ADD COLUMN date_pick_up datetime NULL AFTER dni_pick_up;