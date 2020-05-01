#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

source "${DIR}"/env.sh

"${NEO4J_HOME}"/bin/neo4j stop

"${KAFKA_HOME}"/bin/kafka-server-stop.sh

"${HBASE_HOME}"/bin/stop-hbase.sh

"${ZOOKEEPER_HOME}"/bin/zkServer.sh --config "${ZOOKEEPER_CONF_DIR}" stop
