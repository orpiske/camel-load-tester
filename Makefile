TEST_USER_HOME=/home/$(USER)
TOOLS_HOME:=$(TEST_USER_HOME)/tools
TESTER_DIR=$(TOOLS_HOME)/camel-load-tester/
MVN_PRG:=mvnd

DATA_HOME:=$(TEST_USER_HOME)/data
KAFKA_URI?=dione:9092

CAMEL_VERSIONS?=3.18 3.20 3.21 3.22 4.0 4.1
ANALYZER_VERSION=4.0

.PHONY: all deploy install $(CAMEL_VERSIONS)

dest-dir:
	$(foreach host,$(TEST_HOSTS),ssh $(host) mkdir -p $(TOOLS_HOME)/camel-load-tester/; )

$(CAMEL_VERSIONS): dest-dir
	$(MVN_PRG) -Pcamel-$@ clean package
	$(foreach host,$(TEST_HOSTS),scp camel-load-testers/camel-load-tester-producer/target/camel-load-tester-producer-$@*.jar $(host):$(TESTER_DIR)/camel-load-tester-producer-$@.jar; )
	$(foreach host,$(TEST_HOSTS),scp camel-load-testers/camel-load-tester-consumer/target/camel-load-tester-consumer-$@*.jar $(host):$(TESTER_DIR)/camel-load-tester-consumer-$@.jar; )
	$(foreach host,$(TEST_HOSTS),[[ $@ == $(ANALYZER_VERSION) ]] && scp camel-load-testers/camel-load-tester-analyzer/target/camel-load-tester-analyzer-$@*.jar $(host):$(TESTER_DIR)/camel-load-tester-analyzer.jar || echo "Skipping analyzer for" $@; )


deploy: $(CAMEL_VERSIONS)

