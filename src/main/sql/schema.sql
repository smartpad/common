CREATE DATABASE `smartpad` DEFAULT CHARACTER SET utf8;

use smartpad;

CREATE TABLE `sp_user` (
  `login` varchar(64) NOT NULL,
  `passhash` varchar(256) DEFAULT NULL,
  `branch_id` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `operations` (
  `branch_id` varchar(64) NOT NULL,
  `store_id` varchar(32) NOT NULL DEFAULT '',
  `name` varchar(2048) DEFAULT NULL,
  `schedule` varchar(1024) DEFAULT NULL,
  `address` varchar(1024) DEFAULT NULL,
  `gps_lon` float DEFAULT NULL,
  `gps_lat` float DEFAULT NULL,
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
  `member_levels` varchar(2048) DEFAULT NULL,
  `open_text` varchar(1024) DEFAULT NULL,
  `open_hours` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`branch_id`, `store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
