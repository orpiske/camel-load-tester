CAMEL_VERSION=${2:-"3.12.0-SNAPSHOT"}
PRODUCER_MESSAGE_COUNT=${3:-1000000000}

# mvn exec:java -Dcamel.version=$CAMEL_VERSION -Dcamel.component.kafka.brokers=$1 -Dexec.mainClass="org.apache.camel.kafka.tester.MainProducer"
mvn exec:java -Dcamel.version=$CAMEL_VERSION -Dcamel.main.durationMaxMessages=$PRODUCER_MESSAGE_COUNT -Dtest.file=$CAMEL_VERSION.test -Dcamel.component.kafka.brokers=$1 -Dexec.mainClass="org.apache.camel.kafka.tester.MainProducer"
