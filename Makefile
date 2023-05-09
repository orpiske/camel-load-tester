TEST_HOST?=dione
TEST_USER_HOME=/home/$(USER)
TOOLS_HOME:=$(TEST_USER_HOME)/tools
TESTER_DIR=$(TOOLS_HOME)/camel-load-tester/
MVN_PRG:=mvnd

DATA_HOME:=$(TEST_USER_HOME)/data
KAFKA_URI?=dione:9092
TARGET_CONFIG_DIR=controllers/controller-test/target/config
APPLICATION_PROPERTIES=$(TARGET_CONFIG_DIR)/application.properties
LAUNCHER_CONF=$(TARGET_CONFIG_DIR)/launcher.conf

.PHONY: install

CAMEL_VERSIONS?=3.14 3.18 3.20 4.0.0-M1 4.0.0-M2 4.0.0-M3 4.0
CONTROLLER_VERSION=3.20
ANALYZER_VERSION=3.20

dest-dir:
	ssh $(TEST_HOST) mkdir -p $(TOOLS_HOME)/camel-load-tester/

$(CAMEL_VERSIONS): dest-dir
	$(MVN_PRG) -Pcamel-$@ clean package
	scp camel-load-testers/camel-load-tester-producer/target/camel-load-tester-producer-$@*.jar $(TEST_HOST):$(TESTER_DIR)
	scp camel-load-testers/camel-load-tester-consumer/target/camel-load-tester-consumer-$@*.jar $(TEST_HOST):$(TESTER_DIR)
	@[[ $@ == $(CONTROLLER_VERSION) ]] && scp controllers/controller-test/target/controller-test-$@*.jar $(TEST_HOST):$(TESTER_DIR) || echo "Skipping controller for" $@
	@[[ $@ == $(ANALYZER_VERSION) ]] && scp camel-load-testers/camel-load-tester-analyzer/target/camel-load-tester-analyzer-$@*.jar $(TEST_HOST):$(TESTER_DIR)/camel-load-tester-analyzer.jar || echo "Skipping analyzer for" $@

gen-config:
	@mkdir -p $(TARGET_CONFIG_DIR)
	@echo "camel.component.kafka.brokers="$(KAFKA_URI) > $(APPLICATION_PROPERTIES)
	@echo "common.data.dir="$(DATA_HOME) >> $(APPLICATION_PROPERTIES)
	@echo "common.tester.jvm.start=4G" >> $(APPLICATION_PROPERTIES)
	@echo "common.tester.jvm.max=4G" >> $(APPLICATION_PROPERTIES)
	@echo "common.tester.monitoring=-javaagent:$(TEST_USER_HOME)/tools/prometheus/jmx_prometheus_javaagent.jar=0.0.0.0:9405:$(TESTER_DIR)/config/prometheus/config.yaml" >> $(APPLICATION_PROPERTIES)
	@echo "producer.deployment.dir="$(TESTER_DIR) >> $(APPLICATION_PROPERTIES)
	@echo "consumer.deployment.dir="$(TESTER_DIR) >> $(APPLICATION_PROPERTIES)
	@echo "analyzer.deployment.dir="$(TESTER_DIR) >> $(APPLICATION_PROPERTIES)
	@echo "TESTER_DIR="$(TESTER_DIR) > $(LAUNCHER_CONF)
	@echo "CONTROLLER_VERSION="$(CONTROLLER_VERSION) >> $(LAUNCHER_CONF)

deploy-config:
	ssh $(TEST_HOST) mkdir -p $(TESTER_DIR)/config
	scp $(APPLICATION_PROPERTIES) $(TEST_HOST):$(TESTER_DIR)/config
	scp $(LAUNCHER_CONF) $(TEST_HOST):$(TESTER_DIR)/config

deploy-scripts:
	scp scripts/runner/controller/start-controller.sh $(TEST_HOST):$(TESTER_DIR)
	scp scripts/runner/controller/start-session.sh $(TEST_HOST):$(TESTER_DIR)
	ssh $(TEST_HOST) chmod +x $(TESTER_DIR)/start-controller.sh
	ssh $(TEST_HOST) chmod +x $(TESTER_DIR)/start-session.sh

deploy-prometheus:
	ssh $(TEST_HOST) mkdir -p $(TEST_USER_HOME)/tools/prometheus $(TESTER_DIR)/config/prometheus
	scp resources/prometheus/nodes/config.yaml $(TEST_HOST):$(TESTER_DIR)/config/prometheus
	ssh $(TEST_HOST) wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.17.2/jmx_prometheus_javaagent-0.17.2.jar -O $(TEST_USER_HOME)/tools/prometheus/jmx_prometheus_javaagent-0.17.2.jar
	ssh $(TEST_HOST) ln -sf $(TEST_USER_HOME)/tools/prometheus/jmx_prometheus_javaagent-0.17.2.jar $(TEST_USER_HOME)/tools/prometheus/jmx_prometheus_javaagent.jar

deploy: $(CAMEL_VERSIONS) gen-config deploy-config deploy-scripts

start-session: deploy-scripts
	ssh $(TEST_HOST) tmux new-session -d -s "controller" $(TESTER_DIR)/start-controller.sh

