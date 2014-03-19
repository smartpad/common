CREATE DATABASE `smartpad` DEFAULT CHARACTER SET utf8;

use smartpad;

CREATE TABLE `sp_user` (
  `login` varchar(64) NOT NULL,
  `passhash` varchar(256) DEFAULT NULL,
  `branch_id` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `branch` (
  `branch_id` varchar(64) NOT NULL,
  `name` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `operations` (
  `oper_id` varchar(128) NOT NULL,
  `schedule` varchar(1024) DEFAULT NULL,
  `address` varchar(1024) DEFAULT NULL,
  `phone` varchar(128) DEFAULT NULL,
  `email` varchar(256) DEFAULT NULL,
  `mname_req` tinyint(4) DEFAULT NULL,
  `maddress_req` tinyint(4) DEFAULT NULL,
  `mphone_req` tinyint(4) DEFAULT NULL,
  `mmail_req` tinyint(4) DEFAULT NULL,
  `moffer_free` tinyint(4) DEFAULT NULL,
  `moffer_free_level` int(11) DEFAULT NULL,
  `moffer_survey` varchar(256) DEFAULT NULL,
  `moffer_survey_level` int(11) DEFAULT NULL,
  PRIMARY KEY (`oper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
