CAMEL_VERSION=${2:-"3.12.0-SNAPSHOT"}

mvn exec:java -Dcamel.version=$CAMEL_VERSION -Dcamel.component.kafka.brokers=$1 -Dexec.mainClass="org.apache.camel.kafka.tester.MainProducer"
