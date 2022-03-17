DROP DATABASE IF EXISTS `alkemy_ong`;
CREATE DATABASE IF NOT EXISTS `alkemy_ong`;
USE `alkemy_ong`;

DROP TABLE IF EXISTS `alkemy_ong`.`roles`;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alkemy_ong`.`roles`
(
    `id`          BIGINT(255) NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(20) NOT NULL,
    `description` VARCHAR(50) DEFAULT NULL,
    `createdAt`   TIMESTAMP   DEFAULT NOW(),
    `updateAt`    TIMESTAMP   DEFAULT NOW(),
    `deleted`     BIT         DEFAULT 1,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;

DROP TABLE IF EXISTS `alkemy_ong`.`users`;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alkemy_ong`.`users`
(
    `id`        BIGINT(255)        NOT NULL AUTO_INCREMENT,
    `firstName` VARCHAR(50)        NOT NULL,
    `lastName`  VARCHAR(50)        NOT NULL,
    `email`     VARCHAR(30) UNIQUE NOT NULL,
    `password`  VARCHAR(50)        NOT NULL,
    `photo`     VARCHAR(255) DEFAULT NULL,
    `createdAt` TIMESTAMP    DEFAULT NOW(),
    `updateAt`  TIMESTAMP    DEFAULT NOW(),
    `deleted`   BIT          DEFAULT 1,
    `fk_role`   BIGINT(255)        NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_role` (`fk_role`),
    CONSTRAINT `FK_role` FOREIGN KEY (`fk_role`) REFERENCES `alkemy_ong`.`roles` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;