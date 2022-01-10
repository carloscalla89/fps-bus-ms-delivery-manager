ALTER TABLE `client_fulfillment`
    ADD COLUMN `referral_code` VARCHAR(20) NULL DEFAULT NULL;

ALTER TABLE `client_fulfillment`
    ADD COLUMN `referral_msg` VARCHAR(300) NULL DEFAULT NULL;
