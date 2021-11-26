CAMEL_VERSION=${2:-"3.12.0-SNAPSHOT"}
PRODUCER_MESSAGE_COUNT=${3:-1000000000}

java -jar -Dcamel.version=$CAMEL_VERSION -Dcamel.main.durationMaxMessages=$PRODUCER_MESSAGE_COUNT -Dtest.file=$CAMEL_VERSION.test -Dcamel.component.kafka.brokers=$1 target/kafka-tester-${CAMEL_VERSION}-producer.jar
