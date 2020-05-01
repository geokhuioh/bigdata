#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

source "${DIR}"/env.sh

"${KAFKA_HOME}"/bin/kafka-topics.sh --bootstrap-server localhost:9092 \
  --create \
  --topic click-events \
  --partitions 16
