source ./config/launcher.conf

java -jar ${TESTER_DIR}/controller-test-${CONTROLLER_VERSION}*.jar -Dconfig.dir=/Users/opiske/tmp/test-config
