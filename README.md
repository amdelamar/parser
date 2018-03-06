# parser

A Java program that parses web server access log file, loads the log to MySQL and checks if a given IP makes more than a certain number of requests for the given duration.

### Initial Load

First, load the access log into the database for parsing.

```
java -cp "parser.jar" com.ef.Parser --accesslog=access.log

outputs: 
Uplaoding access log...
Done.
```

It might take a few minutes. BUt now the table is ready to be queried. Below are two examples.

### Example 1

```
java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.15:00:00 --duration=hourly --threshold=200

232 requests from 192.168.106.134
211 requests from 192.168.11.231
```

If you open the log file, 192.168.11.231 has 200 or more requests between 2017-01-01.15:00:00 and 2017-01-01.15:59:59

Test by running this SQL directly:

```sql
select ip, count(*) as count from accesslog where date >= '2017-01-01.15:00:00' and date < '2017-01-01 16:00:00.0' group by ip having count(*) > 200 order by COUNT(*) desc;
```

### Example 2

```
java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.00:00:00 --duration=daily --threshold=500

747 requests from 192.168.129.191
743 requests from 192.168.38.77
729 requests from 192.168.143.177
640 requests from 192.168.199.209
623 requests from 192.168.162.248
610 requests from 192.168.51.205
601 requests from 192.168.203.111
591 requests from 192.168.31.26
584 requests from 192.168.33.16
582 requests from 192.168.62.176
541 requests from 192.168.52.153
536 requests from 192.168.206.141
533 requests from 192.168.219.10
528 requests from 192.168.185.164
513 requests from 192.168.102.136
```
 
If you open the log file, 192.168.102.136 has 500 or more requests between 2017-01-01.00:00:00 and 2017-01-01.23:59:59

Test by running this SQL directly:

```sql
select ip, count(*) as count from accesslog where date >= '2017-01-01.00:00:00' and date < '2017-01-02 00:00:00.0' group by ip having count(*) > 500 order by COUNT(*) desc;
```

## Build Jar

 1. Git clone or download this repo.
 1. `cd parser` into the repo directory.
 1. `gradlew clean build` to build the .jar in /build/libs/
 
### SQL Schema

Schema is stored in `schema.sql` but it looks like this:

```sql
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
```

