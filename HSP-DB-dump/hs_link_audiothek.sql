CREATE DATABASE IF NOT EXISTS `hsdb`;
USE `hsdb`;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for hs_link_audiothek
-- ----------------------------
DROP TABLE IF EXISTS `hs_link_audiothek`;
CREATE TABLE `hs_link_audiothek` (
  `id` INT AUTO_INCREMENT UNIQUE,
  `DUKEY` bigint(20) NOT NULL,
  `AUDIOTHEK_ID` VARCHAR(16) NOT NULL,
  `SCORE` FLOAT DEFAULT 0,
  `AUDIOTHEK_LINK` VARCHAR(256) NOT NULL,
  `VALIDATION_DT` DATETIME NOT NULL,
  `DELETED` TINYINT(1) DEFAULT 0,
  PRIMARY KEY(`DUKEY`, `AUDIOTHEK_ID`),
  KEY `I_DF_DUKEY` (`DUKEY`) USING BTREE,
  KEY `I_DF_AUDIOTHEK_ID` (`AUDIOTHEK_ID`) USING BTREE,
  KEY `I_DF_VALIDATION_DT` (`VALIDATION_DT`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS=1;
