DROP TABLE  IF EXISTS `personas`;
DROP TABLE  IF EXISTS `telefonos`;

CREATE TABLE `personas` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`nombre` varchar(255) NOT NULL,
	PRIMARY KEY (`id`)
);

CREATE TABLE `telefonos` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`numero` varchar(255) NOT NULL,
	`persona_id` INT NOT NULL,
	PRIMARY KEY (`id`)
);

ALTER TABLE `telefonos` ADD CONSTRAINT `telefonos_fk0` FOREIGN KEY (`persona_id`) REFERENCES `personas`(`id`);

INSERT INTO `personas` (id,nombre) values(1000,'primera');
INSERT INTO `telefonos` (id,numero,persona_id) values(2000,'123456',1000);
INSERT INTO `telefonos` (id,numero,persona_id) values(2010,'654321',1000);

