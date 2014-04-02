CREATE DATABASE `smartpad` DEFAULT CHARACTER SET utf8;

use smartpad;

CREATE TABLE `sp_users` (
  `login` varchar(32) NOT NULL,
  `passhash` varchar(32) DEFAULT NULL,
  `branch_id` varchar(32) DEFAULT NULL,
  
  `create_date` datetime NOT NULL,
  `update_date` datetime DEFAULT NULL,
  `create_by` varchar(32) NOT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  
  PRIMARY KEY (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `operations` (

  `oper_id` varchar(32) NOT NULL,
  `branch_id` varchar(32) NOT NULL,
  `syscat_id` varchar(32) NOT NULL,
  
  `name` varchar(1024) NOT NULL,
  `descript` text DEFAULT NULL,
  `images` text DEFAULT NULL,
  
  `create_date` datetime NOT NULL,
  `update_date` datetime DEFAULT NULL,
  `create_by` varchar(32) NOT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  
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
  PRIMARY KEY (`oper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `catalogs` (
  `catalog_id` varchar(32) NOT NULL,
  `parent_id` varchar(32) NOT NULL,
  `branch_id` varchar(32) NOT NULL,
  `syscat_id` varchar(32) DEFAULT NULL,
  
  `name` varchar(1024) NOT NULL,
  `descript` text DEFAULT NULL,
  `images` text DEFAULT NULL,
  `spec` text DEFAULT NULL,
  
  `create_date` datetime NOT NULL,
  `update_date` datetime DEFAULT NULL,
  `create_by` varchar(32) NOT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  
  PRIMARY KEY (`catalog_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `catalog_items` (
  `item_id` varchar(32) NOT NULL,
  `catalog_id` varchar(32) NOT NULL,
  `branch_id` varchar(32) NOT NULL,
  
  `name` varchar(1024) NOT NULL,
  `descript` text DEFAULT NULL,
  `images` text DEFAULT NULL,
  
  `create_date` datetime NOT NULL,
  `update_date` datetime DEFAULT NULL,
  `create_by` varchar(32) NOT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  
  `unit_price` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `promos` (
  `promo_id` varchar(32) NOT NULL,
  `oper_id` varchar(32) NOT NULL,
  `branch_id` varchar(32) NOT NULL,
  
  `create_date` datetime NOT NULL,
  `update_date` datetime DEFAULT NULL,
  `create_by` varchar(32) NOT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  
  `name` varchar(1024) NOT NULL,
  `descript` text DEFAULT NULL,
  `images` text DEFAULT NULL,
  PRIMARY KEY (`promo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
