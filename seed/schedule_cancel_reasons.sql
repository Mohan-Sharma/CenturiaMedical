/*
SQLyog Community v12.09 (64 bit)
MySQL - 5.1.68-community 
*********************************************************************
*/
/*!40101 SET NAMES utf8 */;

create table `schedule_cancel_reasons` (
	`id` bigint (20),
	`reason` varchar (765),
	`description` text 
); 
insert into `schedule_cancel_reasons` (`id`, `reason`, `description`) values('1','Reason A','Reason A');
insert into `schedule_cancel_reasons` (`id`, `reason`, `description`) values('2','Reason A','Reason B');
insert into `schedule_cancel_reasons` (`id`, `reason`, `description`) values('3','Reason A','Reason C');
insert into `schedule_cancel_reasons` (`id`, `reason`, `description`) values('4','Reason A','Reason D');
insert into `schedule_cancel_reasons` (`id`, `reason`, `description`) values('5','Reason A','Reason E');
