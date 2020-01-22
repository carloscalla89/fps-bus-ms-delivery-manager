--
-- Alter table local add pk composite with code and company_code name_service
--
ALTER TABLE `local`
DROP PRIMARY KEY,
ADD PRIMARY KEY (`code`, `company_code`);


