KAFKA_HOME=${HOME}/tools/kafka/bin
BOOTSTRAP_HOST=localhost:9092
ZOOKEEPER_HOST=localhost:2181
TOPIC=test

source "${HOME}"/current.env

"${KAFKA_HOME}"/kafka-producer-perf-test.sh --topic ${TOPIC} --producer-props bootstrap.servers=${BOOTSTRAP_HOST} --throughput -1 --num-records "${CONSUMER_MESSAGE_COUNT}" --record-size "${MESSAGE_SIZE}" || true
notify-pushover "Finished producing ${CONSUMER_MESSAGE_COUNT} test data"
echo "Load completed"
exit 0

