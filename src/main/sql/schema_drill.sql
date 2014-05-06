
CREATE TABLE `clusters` (

  `cluster_id` int not null,
  
  PRIMARY KEY (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `operations_clusters` (

  `cluster_id` int default null,
  `cluster_rank` int default null,
  
  --`open_start` datetime default null,
  --`open_end` datetime default null,
  
  --identical to smartpad.operations

  `store_id` varchar(32) NOT NULL,
  `branch_id` varchar(32) NOT NULL,
  `syscat_id` varchar(32) NOT NULL,
  
  `branch_name` varchar(2048) NOT NULL,
  
  `name` varchar(2048) NOT NULL,
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
  `open_hours` varchar(2048) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `promos_clusters` (

  `cluster_id` int default null,
  `cluster_rank` int default null,
  
  `visa_c` boolean default false,
  `visa_c_issuers` varchar(1024) default null,
  `visa_d` boolean default false,
  `visa_d_issuers` varchar(1024) default null,
  
  `master_c` boolean default false,
  `master_c_issuers` varchar(1024) default null,
  `master_d` boolean default false,
  `master_d_issuers` varchar(1024) default null,
  
  --`schedule_start` datetime default null,
  --`schedule_end` datetime default null,
  
  --identical to smartpad.promos
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
  `update_by` varchar(32) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `promos_alerts` (

  `consumer_id` varchar(32) NOT NULL,

  --identical to promos
  `promo_id` varchar(32) NOT NULL,
  `store_id` varchar(32) NOT NULL,
  `branch_id` varchar(32) NOT NULL,
  `syscat_id` varchar(32) NOT NULL,
  
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
  `update_by` varchar(32) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
