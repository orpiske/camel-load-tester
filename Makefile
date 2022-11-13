TEST_HOST?=dione
TEST_USER_HOME=/home/$(USER)
TOOLS_HOME?=$(TEST_USER_HOME)/tools

.PHONY: install

CAMEL_VERSIONS?=3.14 3.18 3.19 3.20

dest-dir:
	ssh $(TEST_HOST) mkdir -p $(TOOLS_HOME)/kafka-tester/

$(CAMEL_VERSIONS): dest-dir
	mvn -Pcamel-$@ clean package
	scp kafka-testers/kafka-tester-producer/target/kafka-tester-producer-$@*.jar $(TEST_HOST):$(TOOLS_HOME)/kafka-tester/
	scp kafka-testers/kafka-tester-consumer/target/kafka-tester-consumer-$@*.jar $(TEST_HOST):$(TOOLS_HOME)/kafka-tester/

install-consumer-tests:
	rsync -avr --progress --stats scripts/runner/consumer $(TEST_HOST):$(TOOLS_HOME)

install-producer-tests:
	rsync -avr --progress --stats scripts/runner/producer $(TEST_HOST):$(TOOLS_HOME)

install-tests: install-producer-tests install-consumer-tests

deploy: $(CAMEL_VERSIONS) install-tests

start-consumer-test-session:
	ssh $(TEST_HOST) touch $(TEST_USER_HOME)/block.file
	ssh $(TEST_FILE) ${TEST_HOST} tmux new-session -d -s "consumer" $(TOOLS_HOME)/consumer/run-test-consumer.sh

start-producer-test-session:
	ssh $(TEST_HOST) touch $(TEST_USER_HOME)/block.file
	ssh $(TEST_FILE) ${TEST_HOST} tmux new-session -d -s "producer" $(TOOLS_HOME)/producer/run-test-producer.sh


