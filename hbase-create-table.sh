#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

source "${DIR}"/env.sh

"${HBASE_HOME}"/bin/hbase shell "${DIR}/hbase-create-table.txt"
