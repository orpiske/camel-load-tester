TEST_HOST?=dione
TEST_USER_HOME=/home/$(USER)
TOOLS_HOME:=$(TEST_USER_HOME)/tools
TESTER_DIR=$(TOOLS_HOME)/kafka-tester/

DATA_HOME:=$(TEST_USER_HOME)/data
KAFKA_URI?=dione:9092
TARGET_CONFIG_DIR=controllers/controller-test/target/config
APPLICATION_PROPERTIES=$(TARGET_CONFIG_DIR)/application.properties
LAUNCHER_CONF=$(TARGET_CONFIG_DIR)/launcher.conf

.PHONY: install

CAMEL_VERSIONS?=3.18 3.20 4.0
CONTROLLER_VERSION=3.20
ANALYZER_VERSION=3.20

dest-dir:
	ssh $(TEST_HOST) mkdir -p $(TOOLS_HOME)/kafka-tester/

$(CAMEL_VERSIONS): dest-dir
	mvn -Pcamel-$@ clean package
	scp kafka-testers/kafka-tester-producer/target/kafka-tester-producer-$@*.jar $(TEST_HOST):$(TESTER_DIR)
	scp kafka-testers/kafka-tester-consumer/target/kafka-tester-consumer-$@*.jar $(TEST_HOST):$(TESTER_DIR)
	scp kafka-testers/kafka-tester-analyzer/target/kafka-tester-analyzer-$@*.jar $(TEST_HOST):$(TESTER_DIR)
	scp controllers/controller-test/target/controller-test-$@*.jar $(TEST_HOST):$(TESTER_DIR)

gen-config:
	@mkdir -p $(TARGET_CONFIG_DIR)
	@echo "camel.component.kafka.brokers="$(KAFKA_URI) > $(APPLICATION_PROPERTIES)
	@echo "common.data.dir="$(DATA_HOME) >> $(APPLICATION_PROPERTIES)
	@echo "common.tester.jvm.start=4G" >> $(APPLICATION_PROPERTIES)
	@echo "common.tester.jvm.max=4G" >> $(APPLICATION_PROPERTIES)

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
	ssh $(TEST_HOST) chmod +x $(TESTER_DIR)/start-controller.sh

deploy: $(CAMEL_VERSIONS) gen-config deploy-config

start-session:
	ssh $(TEST_FILE) ${TEST_HOST} tmux new-session -d -s "controller" $(TESTER_DIR)/start-controller.sh

start-producer-test-session:
	ssh $(TEST_HOST) touch $(TEST_USER_HOME)/block.file
	ssh $(TEST_FILE) ${TEST_HOST} tmux new-session -d -s "producer" $(TOOLS_HOME)/producer/run-test-producer.sh


