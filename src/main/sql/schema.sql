--CREATE DATABASE `smartpad` DEFAULT CHARACTER SET utf8;

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

CREATE TABLE `consumers` (
  `consumer_id` varchar(32) NOT NULL,
  `passhash` varchar(32) DEFAULT NULL,
  
  `cluster_id` int,
  
  `create_date` datetime NOT NULL,
  `update_date` datetime DEFAULT NULL,
  `create_by` varchar(32) NOT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  
  PRIMARY KEY (`consumer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `operations` (

  `store_id` varchar(32) NOT NULL,
  `branch_id` varchar(32) NOT NULL,
  `syscat_id` varchar(32) NOT NULL,
  
  `branch_name` varchar(2048) NOT NULL,
  
  `name` varchar(2048) NOT NULL,
  `name_md` varchar(32) NOT NULL unique,
  `descript` text DEFAULT NULL,
  `images` text DEFAULT NULL,
  
  `create_date` datetime NOT NULL,
  `update_date` datetime DEFAULT NULL,
  `create_by` varchar(32) NOT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  
  `gps_lon` decimal(9,6) DEFAULT NULL,
  `gps_lat` decimal(9,6) DEFAULT NULL,
  `gps_inherit` varchar(8) NOT null,
  
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
  `member_levels` varchar(2048) DEFAULT NULL,
  `open_hours` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `catalogs` (
  `catalog_id` varchar(32) NOT NULL,
  `parent_id` varchar(32) NOT NULL,
  `parent_cross_id` varchar(32) DEFAULT NULL,
  `store_id` varchar(32) NOT NULL,
  `branch_id` varchar(32) NOT NULL,
  `syscat_id` varchar(32) DEFAULT NULL,
  `partial_id` int NOT NULL,
  
  `branch_name` varchar(2048) NOT NULL,
  `parent_name` varchar(1024) NOT NULL,
  `segments` text DEFAULT NULL,
  
  `gps_lon` decimal(9,6) DEFAULT NULL,
  `gps_lat` decimal(9,6) DEFAULT NULL,
  `gps_inherit` varchar(8) default null,
  
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

CREATE TABLE `promos` (
  `promo_id` varchar(32) NOT NULL,
  `store_id` varchar(32) NOT NULL,
  `branch_id` varchar(32) NOT NULL,
  `syscat_id` varchar(32) NOT NULL,
  
  --used by drilling only
  `ended` boolean default false,
  
  `name` varchar(1024) NOT NULL,
  `descript` text DEFAULT NULL,
  `images` text DEFAULT NULL,
  
  `member_level` int,
  `member_point` int,
  `ccard_req` text default null,
  `schedule` text default null,
  
  `gps_lon` decimal(9,6) DEFAULT NULL,
  `gps_lat` decimal(9,6) DEFAULT NULL,
  `gps_inherit` varchar(8) default null,
  
  `create_date` datetime NOT NULL,
  `update_date` datetime DEFAULT NULL,
  `create_by` varchar(32) NOT NULL,
  `update_by` varchar(32) DEFAULT NULL,
  
  PRIMARY KEY (`promo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `similars_ver` (
  `type_id` varchar(8) NOT NULL,
  `version` varchar(2) NOT NULL,
  PRIMARY KEY (`type_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `similars` (
  `target` varchar(42) NOT NULL,
  `json` text NOT NULL,
  PRIMARY KEY (`target`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;