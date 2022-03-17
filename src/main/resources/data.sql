USE `alkemy_ong`;

LOCK TABLES `alkemy_ong`.`roles` WRITE;
INSERT INTO `alkemy_ong`.`roles` (name, description, createdAt,updateAt) VALUES ('ADMIN', 'System administrator',NOW(),NOW());
UNLOCK TABLES;

LOCK TABLES `alkemy_ong`.`users` WRITE;
/*!40000 ALTER TABLE `alkemy_ong`.`users` DISABLE KEYS */;
INSERT INTO `alkemy_ong`.`users` (firstName, lastName, email, password,photo, createdAt, updateAt, deleted, fk_role)
VALUES ('Juan','Perez','juan@mail.com',MD5('1234'),'',NOW(),NOW(),0,1);
/*!40000 ALTER TABLE `alkemy_ong`.`users` ENABLE KEYS */;
UNLOCK TABLES;