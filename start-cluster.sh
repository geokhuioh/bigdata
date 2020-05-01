#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

source "${DIR}"/env.sh

"${ZOOKEEPER_HOME}"/bin/zkServer.sh --config "${ZOOKEEPER_CONF_DIR}" start

"${HBASE_HOME}"/bin/start-hbase.sh

"${KAFKA_HOME}"/bin/kafka-server-start.sh -daemon "${KAFKA_CONF_DIR}"/server.properties

"${NEO4J_HOME}"/bin/neo4j start
