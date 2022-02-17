TEST_HOST=dione

CAMEL_VERSIONS=3.11.5 3.14.0 3.15.0 3.16.0

dest-dir:
	ssh $(TEST_HOST) mkdir -p /home/$(USER)/tools

$(CAMEL_VERSIONS): dest-dir
	mvn -Pcamel-$@ clean package
	scp kafka-testers/kafka-tester-producer/target/kafka-tester-producer-$@*.jar $(TEST_HOST):/home/$(USER)/tools/kafka-tester
	scp kafka-testers/kafka-tester-consumer/target/kafka-tester-consumer-$@.jar $(TEST_HOST):/home/$(USER)/tools/kafka-tester

deploy: $(CAMEL_VERSIONS)
