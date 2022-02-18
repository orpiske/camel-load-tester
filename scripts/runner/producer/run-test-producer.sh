TEST_HOME=$HOME/tools/kafka-tester
BOOTSTRAP_HOST=localhost:9092
ZOOKEEPER_HOST=localhost:2181
KAFKA_HOME=$HOME/tools/kafka
TOPIC=test

source ${TEST_HOME}/producer.env

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

echo "Starting the test producer ..."
cd ${TEST_HOME}
while true ; do
	if [[ ! -f ${HOME}/block.file ]]  ; then
		touch ${HOME}/block.file

		unset JAVA_OPTS
		source ${HOME}/current.env
		mkdir -p ${HOME}/test-data/

		echo -e "\nStopping Kafka"
		sudo systemctl stop kafka@$USER
		sleep 5s
		echo "Stopping Zookeeper"
		sudo systemctl stop zookeeper@$USER
		sleep 2s

		echo "Removing Zookeeper data dir"
		rm -rf ${HOME}/var/zookeeper

		echo "Removing Kafka data dir"
		rm -rf ${HOME}/var/kafka-logs

		echo "Starting Zookeeper"
		sudo systemctl start zookeeper@$USER
		sleep 5s
		echo "Starting Kafka"
		sudo systemctl start kafka@$USER
		sleep 2s

		# ${KAFKA_HOME}/bin/kafka-topics.sh --zookeeper ${ZOOKEEPER_HOST} --delete --topic ${TOPIC} || true

		startTime=$(date)

		echo "Starting the test at ${startTime}"
		java ${JAVA_OPTS} -Dcamel.version=${CAMEL_VERSION} \
			-Dcamel.main.durationMaxMessages=${PRODUCER_MESSAGE_COUNT} \
			-Dtest.file="${CAMEL_VERSION}".test -Dcamel.component.kafka.brokers=${BOOTSTRAP_HOST} \
			-jar kafka-tester-producer-"${CAMEL_VERSION}".jar

		mv producer-rate.data ${HOME}/test-data/producer-rate-${TEST_NAME}-${CAMEL_VERSION}.data
		mv producer-latencies.hdr ${HOME}/test-data/producer-latencies-${TEST_NAME}-${CAMEL_VERSION}.hdr
		sendNotification "Producer test for ${CAMEL_VERSION} completed (${PRODUCER_MESSAGE_COUNT} messages)"

		rm -f ${HOME}/current.env || true
	else
		sleep 5s
		currentTime=$(date)
		echo -e -n "\rWaiting for tests: ${currentTime}"
	fi
done
