/*
Navicat MySQL Data Transfer

Source Server         : mine
Source Server Version : 80017
Source Host           : localhost:3306
Source Database       : octopus

Target Server Type    : MYSQL
Target Server Version : 80017
File Encoding         : 65001

Date: 2019-09-09 23:57:50
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for lotinfo
-- ----------------------------
DROP TABLE IF EXISTS `lotinfo`;
CREATE TABLE `lotinfo` (
  `lotid` varchar(20) NOT NULL,
  `starttime` varchar(20) NOT NULL,
  `lotnum` varchar(5) NOT NULL,
  `complete` int(1) NOT NULL,
  `CREATE_TIME` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `UPDATE_TIME` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`lotid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lotinfobak`;
CREATE TABLE `lotinfobak` (
  `lotid` varchar(20) NOT NULL,
  `starttime` varchar(20) NOT NULL,
  `lotnum` varchar(5) NOT NULL,
  `complete` int(1) NOT NULL,
  `CREATE_TIME` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `UPDATE_TIME` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`lotid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
