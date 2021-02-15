CREATE TABLE `payment_method_type` (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `payment_method_id` bigint(20) NOT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO payment_method_type values (1,'CASH_DOLAR', 1);
INSERT INTO payment_method_type values (2,'CASH', 1);
INSERT INTO payment_method_type values (3,'CARD',2);
INSERT INTO payment_method_type values (4,'MARKETPLACE', 5);
INSERT INTO payment_method_type values (5,'ONLINE_PAYMENT', 3);

CREATE TABLE `card_provider` (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `card_providerid` bigint(20) NOT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO card_provider values (1,'American Express',10);
INSERT INTO card_provider values (2,'AMEX crédito',3);
INSERT INTO card_provider values (3,'Dinners débito/crédito',6);
INSERT INTO card_provider values (4,'Mastercard débito/crédito',2);
INSERT INTO card_provider values (5,'Tarjeta OH!',11);
INSERT INTO card_provider values (6,'TARJETA OH! Mastercard',5);
INSERT INTO card_provider values (7,'TARJETA OH! Visa',4);
INSERT INTO card_provider values (8,'Visa débito/crédito',1);