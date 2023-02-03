install_path=$(dirname $0)

source  ${install_path}/config/launcher.conf

java -Dconfig.dir=${TESTER_DIR}/config/ -jar ${TESTER_DIR}/controller-test-${CONTROLLER_VERSION}*.jar
