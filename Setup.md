# Cluster Setup Guide

## Zookeeper

1. [Download](https://zookeeper.apache.org/releases.html#download) Apache Zookeeper 

2. Unzip the downloaded file to `/opt/apache/zookeeper`. This will be referred as `ZOOKEEPER_HOME`

3. Copy `$ZOOKEEPER_HOME/conf/zoo_sample.cfg` to `$ZOOKEEPER_HOME/conf/zoo.cfg`.

4. Open `ZOOKEEPER_HOME/conf/zoo.cfg` and edit the following line

```
dataDir=/var/lib/zookeeper
```

## HBase

1. [Download](https://www.apache.org/dyn/closer.lua/hbase/2.2.4/hbase-2.2.4-bin.tar.gz) Apache HBase

2. Unzip the downloaded file to `/opt/apache/hbase`. This will be referred as `HBASE_HOME`.

3. Edit `$HBASE_HOME/conf/hbase-site.xml`

```aidl
<configuration>
  <property>
    <name>hbase.cluster.distributed</name>
    <value>true</value>
  </property>
  <property>
    <name>hbase.rootdir</name>
    <value>file:///var/lib/hbase</value>
  </property>
  <property>
    <name>hbase.zookeeper.quorum</name>
    <value>127.0.0.1:2181</value>
  </property>
</configuration>

```

## Kafka

1. [Download](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.4.1/kafka_2.12-2.4.1.tgz) Apache Kafka.

2. Extract the file to `/opt/apache/kafka`. This will be referred as `KAFKA_HOME`.

3. Edit `$KAKFA_HOME/config/server.properties` to ensure the following value

```
log.dirs=/var/lib/kafka/kafka-logs
```
