TEST_NAME=$1
BASELINE_VERSION=$2
TEST_VERSION=$3
DATA_DIR=${4:-${HOME}/tmp/kafka-tester-data}

ANALYZER_HOME=../../../kafka-testers/kafka-tester-analyzer
ANALYZER_PATH=${ANALYZER_HOME}/target/kafka-tester-analyzer-3.*.jar

JAR_FILE=$(ls -1 ${ANALYZER_PATH})

java -Dbaseline.rate.file=${DATA_DIR}/consumer-rate-${TEST_NAME}-${BASELINE_VERSION}.data \
	-Dbaseline.latencies.file=${DATA_DIR}/consumer-latencies-${TEST_NAME}-${BASELINE_VERSION}.hdr \
	-Dtest.rate.file=${DATA_DIR}/consumer-rate-${TEST_NAME}-${TEST_VERSION}.data \
	-Dtest.name=${TEST_NAME} \
	-jar ${JAR_FILE}

mkdir ${TEST_NAME}-consumer-report
mv consumer_rate.png ${TEST_NAME}-consumer-report
mv report.html ${TEST_NAME}-consumer-report
