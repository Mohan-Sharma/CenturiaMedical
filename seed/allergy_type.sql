
/*Data for the table `allergy_type` */

insert  into `allergy_type`(`ID`,`IS_ACTIVE`,`CREATED_BY`,`CREATE_TX_TIMESTAMP`,`DEACTIVATION_REASON`,`UPDATE_BY`,`UPDATED_TX_TIMESTAMP`,`VERSION`,`CODE`,`DESCRIPTION`,`PRACTICE_ID`) values 
(1,'','samir','2011-01-11 13:10:32',NULL,'seed','2011-01-11 13:10:32',1,'ALLERGY','Allergy',null),
(2,'','seed','2011-01-11 13:10:55',NULL,'seed','2011-01-11 13:10:55',1,'ADVERSEREACTION','Adverse Reaction',null),
(3,'','seed','2011-01-11 17:40:31',NULL,'seed','2011-01-11 17:40:31',1,'ANIMALALLERGY','Animal Allergy',null),
(4,'','seed','2011-01-11 17:40:48',NULL,'seed','2011-01-11 17:40:48',1,'DA','Drug Allergy',null),
(5,'','seed','2011-01-11 17:41:07',NULL,'seed','2011-01-11 17:41:07',1,'EA','Environment Allergy',null),
(6,'','seed','2011-01-11 17:41:24',NULL,'seed','2011-01-11 17:41:24',1,'FA','Food Allergy',null),
(7,'','seed','2011-01-11 17:41:39',NULL,'seed','2011-01-11 17:41:39',1,'LA','Pollen Allergy',null),
(8,'','seed','2011-01-11 17:41:59',NULL,'seed','2011-01-11 17:41:59',1,'MA','Miscellaneous Allergy',null),
(9,'','seed','2011-01-11 17:42:24',NULL,'seed','2011-01-11 17:42:24',1,'MC','Miscellaneous Contraindication',null),
(10,'','seed','2011-01-11 17:43:02',NULL,'seed','2011-01-11 17:43:02',1,'PA','Plant Allergy',null);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
