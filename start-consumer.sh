CAMEL_VERSION=${2:-"3.12.0-SNAPSHOT"}
CONSUMER_MESSAGE_COUNT=${3:-1000000000}
CONSUMER_COUNT=${4:-1}
PROJECT_VERSION=1.0.0-SNAPSHOT

if [[ $CAMEL_VERSION == "3.11.2" ]] ; then
	java -jar -Dcamel.version=$CAMEL_VERSION -Dcamel.main.durationMaxMessages=$CONSUMER_MESSAGE_COUNT -Dcamel.component.kafka.consumer-streams=$CONSUMER_COUNT -Dcamel.component.kafka.consumers-count=$CONSUMER_COUNT -Dtest.file=$CAMEL_VERSION.test -Dcamel.component.kafka.brokers=$1 target/kafka-tester-${PROJECT_VERSION}-consumer.jar
else
	java -jar -Dcamel.version=$CAMEL_VERSION -Dcamel.main.durationMaxMessages=$CONSUMER_MESSAGE_COUNT -Dcamel.component.kafka.consumers-count=$CONSUMER_COUNT -Dtest.file=$CAMEL_VERSION.test -Dcamel.component.kafka.brokers=$1 target/kafka-tester-${PROJECT_VERSION}-consumer.jar
fi


