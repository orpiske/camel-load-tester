TEST_HOST?=dione
TEST_USER_HOME=/home/$(USER)
TOOLS_HOME:=$(TEST_USER_HOME)/tools
TESTER_DIR=$(TOOLS_HOME)/camel-load-tester/
MVN_PRG:=mvnd

DATA_HOME:=$(TEST_USER_HOME)/data
KAFKA_URI?=dione:9092

.PHONY: install

CAMEL_VERSIONS?=3.18 3.20 3.21 4.0.0-M1 4.0.0-M2 4.0.0-M3 4.0.0-RC2 4.0
CONTROLLER_VERSION=3.20
ANALYZER_VERSION=3.20

dest-dir:
	ssh $(TEST_HOST) mkdir -p $(TOOLS_HOME)/camel-load-tester/

$(CAMEL_VERSIONS): dest-dir
	$(MVN_PRG) -Pcamel-$@ clean package
	scp camel-load-testers/camel-load-tester-producer/target/camel-load-tester-producer-$@*.jar $(TEST_HOST):$(TESTER_DIR)
	scp camel-load-testers/camel-load-tester-consumer/target/camel-load-tester-consumer-$@*.jar $(TEST_HOST):$(TESTER_DIR)
	@[[ $@ == $(ANALYZER_VERSION) ]] && scp camel-load-testers/camel-load-tester-analyzer/target/camel-load-tester-analyzer-$@*.jar $(TEST_HOST):$(TESTER_DIR)/camel-load-tester-analyzer.jar || echo "Skipping analyzer for" $@


deploy: $(CAMEL_VERSIONS) deploy-config deploy-scripts

