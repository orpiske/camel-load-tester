TEST_HOST?=dione
TOOLS_HOME?=/home/$(USER)/tools

install-consumer-tests:
	rsync -avr --progress --stats consumer $(TEST_HOST):$(TOOLS_HOME)

install-producer-tests:
	rsync -avr --progress --stats producer $(TEST_HOST):$(TOOLS_HOME)

start-consumer-test-session:
	ssh $(TEST_HOST) touch $(HOME)/block.file
	ssh $(TEST_FILE) ${TEST_HOST} tmux new-session -d -s "consumer" $(TOOLS_HOME)/consumer/run-test-consumer.sh

start-producer-test-session:
	ssh $(TEST_HOST) touch $(HOME)/block.file
	ssh $(TEST_FILE) ${TEST_HOST} tmux new-session -d -s "producer" $(TOOLS_HOME)/producer/run-test-producer.sh

