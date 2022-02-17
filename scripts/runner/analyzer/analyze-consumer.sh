TEST_NAME=$1
BASELINE_VERSION=$2
TEST_VERSION=$3
DATA_DIR=$4

ANALYZER_HOME=/home/opiske/code/test/kafka-tester/kafka-testers/kafka-tester-analyzer/target
ANALYZER_PATH=${ANALYZER_HOME}/kafka-tester-analyzer-3.14.0-SNAPSHOT.jar

java -Dbaseline.rate.file=${DATA_DIR}/consumer-rate-${TEST_NAME}-${BASELINE_VERSION}.data \
	-Dbaseline.latencies.file=${DATA_DIR}/consumer-latencies-${TEST_NAME}-${BASELINE_VERSION}.hdr \
	-Dtest.rate.file=${DATA_DIR}/consumer-rate-${TEST_NAME}-${TEST_VERSION}.data \
	-Dtest.name=${TEST_NAME} \
	-jar ${ANALYZER_PATH}

mkdir ${TEST_NAME}-consumer-report
mv consumer_rate.png ${TEST_NAME}-consumer-report
mv report.html ${TEST_NAME}-consumer-report
