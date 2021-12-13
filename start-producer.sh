CAMEL_VERSION=${2:-"3.12.0-SNAPSHOT"}
PRODUCER_MESSAGE_COUNT=${3:-1000000000}

java -jar -Dcamel.version=$CAMEL_VERSION -Dcamel.main.durationMaxMessages=$PRODUCER_MESSAGE_COUNT -Dtest.rate.file=$CAMEL_VERSION-producer-rate.data -Dtest.latencies.file=$CAMEL_VERSION-producer-latencies.hdr -Dcamel.component.kafka.brokers=$1 target/kafka-tester-${CAMEL_VERSION}-producer.jar
