KAFKA_HOME=${HOME}/tools/kafka/bin
BOOTSTRAP_HOST=localhost:9092
ZOOKEEPER_HOST=localhost:2181
TOPIC=test

source "${HOME}"/current.env

function sendNotification() {
  which notify-pushover &> /dev/null
  if [ $? -eq 0 ] ; then
    notify-pushover "$1"
  else
    which notify-send  &> /dev/null
    if [ $? -eq 0 ] ; then
      notify-send -t 2000 "$1"
    else
      echo "$1"
    fi
  fi
}

"${KAFKA_HOME}"/kafka-producer-perf-test.sh --topic ${TOPIC} --producer-props bootstrap.servers=${BOOTSTRAP_HOST} --throughput -1 --num-records "${CONSUMER_MESSAGE_COUNT}" --record-size "${MESSAGE_SIZE}" || true
sendNotification "Finished producing ${CONSUMER_MESSAGE_COUNT} test data"
echo "Load completed"
exit 0

