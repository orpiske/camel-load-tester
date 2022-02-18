LOAD_TOOL_HOME=$HOME/tools/consumer
TEST_HOME=$HOME/tools/kafka-tester
BOOTSTRAP_HOST=localhost:9092

source "${TEST_HOME}"/consumer.env

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


echo "Starting the test consumer ..."
cd "${TEST_HOME}" || exit 1
while true ; do
	if [[ ! -f ${HOME}/block.file ]]  ; then
		touch "${HOME}"/block.file

		unset JAVA_OPTS
		source "${HOME}"/current.env
		mkdir -p "${HOME}"/test-data/

		echo -e "\nStopping Kafka"
		sudo systemctl stop kafka@$USER
		sleep 5s
		echo "Stopping Zookeeper"
		sudo systemctl stop zookeeper@$USER
		sleep 2s

		echo "Removing Zookeeper data dir"
		rm -rf "${HOME}"/var/zookeeper

		echo "Removing Kafka data dir"
		rm -rf "${HOME}"/var/kafka-logs

		echo "Starting Zookeeper"
		sudo systemctl start zookeeper@$USER
		sleep 5s
		echo "Starting Kafka"
		sudo systemctl start kafka@$USER
		sleep 2s

		"${LOAD_TOOL_HOME}"/load-test-data.sh

		sleep 2s

		startTime=$(date)

		echo "Starting the test at ${startTime}"
		if [[ $CAMEL_VERSION == *"3.11"* ]] ; then
			echo "Using Camel 3.11 specific settings"
			java "${JAVA_OPTS}" -Dcamel.version=${CAMEL_VERSION} \
				-Dcamel.main.durationMaxMessages=${CONSUMER_MESSAGE_COUNT} \
				-Dcamel.component.kafka.consumer-streams=${CONSUMERS} \
				-Dcamel.component.kafka.consumers-count=${CONSUMERS} \
				-Dcamel.component.kafka.brokers=${BOOTSTRAP_HOST} \
				-jar kafka-tester-consumer-"${CAMEL_VERSION}".jar
		else
			java ${JAVA_OPTS} -Dcamel.version=${CAMEL_VERSION} \
				-Dcamel.main.durationMaxMessages=${CONSUMER_MESSAGE_COUNT} \
				-Dcamel.component.kafka.consumers-count=${CONSUMERS} \
				-Dcamel.component.kafka.brokers=${BOOTSTRAP_HOST} \
				-jar kafka-tester-consumer-"${CAMEL_VERSION}".jar
		fi

		mv consumer-rate.data "${HOME}"/test-data/consumer-rate-${TEST_NAME}-${CAMEL_VERSION}.data

		sendNotification "Consumer test for ${CAMEL_VERSION} with ${CONSUMERS} consumers completed (${CONSUMER_MESSAGE_COUNT} messages)"

		rm -f ${HOME}/current.env || true
	else
		sleep 5s
		currentTime=$(date)
		echo -e -n "\rWaiting for tests: ${currentTime}"
	fi
done
