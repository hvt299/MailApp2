CREATE DATABASE `mailserver`;

USE `mailserver`;

CREATE TABLE `account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `pwd` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `mail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_sent` int NOT NULL,
  `id_receive` int NOT NULL,
  `title` varchar(45) NOT NULL,
  `content` varchar(45) NOT NULL,
  `date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_mail_account_id_receive_idx` (`id_receive`,`id_sent`),
  KEY `FK_mail_account_id_send_idx` (`id_sent`),
  CONSTRAINT `FK_mail_account_id_receive` FOREIGN KEY (`id_receive`) REFERENCES `account` (`id`),
  CONSTRAINT `FK_mail_account_id_send` FOREIGN KEY (`id_sent`) REFERENCES `account` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;