CAMEL_VERSION=${2:-"3.12.0-SNAPSHOT"}
PRODUCER_MESSAGE_COUNT=${3:-1000000000}
PROJECT_VERSION=1.0.0-SNAPSHOT

java -jar -Dcamel.version=$CAMEL_VERSION -Dcamel.main.durationMaxMessages=$PRODUCER_MESSAGE_COUNT -Dtest.file=$CAMEL_VERSION.test -Dcamel.component.kafka.brokers=$1 target/kafka-tester-${PROJECT_VERSION}-producer.jar
