name := "bigdata"

version := "1.0"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "2.4.5",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.5",
  "org.apache.spark" %% "spark-avro" % "2.4.5",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.5",
  "org.apache.spark" %% "spark-streaming" % "2.4.5" excludeAll(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.module")
  ),
  "org.apache.hbase.connectors.spark" % "hbase-spark" % "1.0.0" excludeAll(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.module")
  ),
  "org.neo4j.driver" % "neo4j-java-driver" % "4.0.0",
  "org.apache.hbase" % "hbase-common" % "2.1.0",
  "org.apache.hbase" % "hbase-server" % "2.1.0",
  "org.apache.hbase" % "hbase-hadoop-compat" % "2.1.0",
  "org.apache.hbase" % "hbase-hadoop2-compat" % "2.1.0",
  "org.apache.hbase" % "hbase-metrics" % "2.1.0",
  "org.apache.hbase" % "hbase-metrics-api" % "2.1.0",
  "org.apache.hbase" % "hbase-zookeeper" % "2.1.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.2",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.9.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.2",
  "com.fasterxml.jackson.module" % "jackson-module-paranamer" % "2.9.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.2"
)

resolvers += "Spark Packages" at "https://dl.bintray.com/spark-packages/maven/"
