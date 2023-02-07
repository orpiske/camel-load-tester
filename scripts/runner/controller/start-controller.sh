TOOLS_HOME=${HOME}/tools
install_path=$(dirname $0)

source  ${install_path}/config/launcher.conf

java -Dconfig.dir=${TESTER_DIR}/config/ \
  -javaagent:${TOOLS_HOME}/prometheus/jmx_prometheus_javaagent.jar=0.0.0.0:9404:${install_path}/config/prometheus/config.yaml \
  -jar ${TESTER_DIR}/controller-test-${CONTROLLER_VERSION}*.jar
