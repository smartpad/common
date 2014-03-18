CREATE DATABASE `smartpad` /*!40100 DEFAULT CHARACTER SET latin1 */;

CREATE TABLE `sp_user` (
  `login` varchar(256) NOT NULL,
  `passhash` varchar(256) DEFAULT NULL,
  `branch_id` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `branch` (
  `branch_id` varchar(256) NOT NULL,
  `name` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
