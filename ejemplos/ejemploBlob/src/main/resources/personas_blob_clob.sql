DROP TABLE IF EXISTS personasfoto;
CREATE TABLE `personasfoto` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`nombre` varchar(255) NOT NULL,
	`foto` blob,
        `documento` longtext,
	PRIMARY KEY (`id`)
);

