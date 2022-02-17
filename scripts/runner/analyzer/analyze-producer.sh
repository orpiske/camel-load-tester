TEST_NAME=$1
BASELINE_VERSION=$2
TEST_VERSION=$3
DATA_DIR=$4

ANALYZER_PATH=/home/opiske/code/test/kafka-tester/kafka-testers/kafka-tester-analyzer/target/kafka-tester-analyzer-3.14.0-SNAPSHOT.jar

java -Dbaseline.rate.file=${DATA_DIR}/producer-rate-${TEST_NAME}-${BASELINE_VERSION}.data \
	-Dbaseline.latencies.file=${DATA_DIR}/producer-latencies-${TEST_NAME}-${BASELINE_VERSION}.hdr \
	-Dtest.rate.file=${DATA_DIR}/producer-rate-${TEST_NAME}-${TEST_VERSION}.data \
	-Dtest.latencies.file=${DATA_DIR}/producer-latencies-${TEST_NAME}-${TEST_VERSION}.hdr \
	-Dtest.name=${TEST_NAME} \
	-jar ${ANALYZER_PATH}

mkdir ${TEST_NAME}-producer-report
mv producer_rate.png ${TEST_NAME}-producer-report
mv latency_all.png ${TEST_NAME}-producer-report
mv report.html ${TEST_NAME}-producer-report