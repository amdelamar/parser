CREATE DATABASE `weblog`;

CREATE TABLE IF NOT EXISTS accesslog (
  id mediumint(9) NOT NULL AUTO_INCREMENT,
  start_date timestamp NOT NULL,
  ipv4 varchar(15) NOT NULL,
  http_method varchar(20) NOT NULL,
  http_code smallint(15) NOT NULL,
  useragent varchar(300) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (id)
);