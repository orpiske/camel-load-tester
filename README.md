Camel Kafka Mock Tester
=========================

Introduction
===

Simple Camel Kafka mock consumer for computing the consumption rate for the Camel Kafka component. It also comes with a low rate producer to aid with debugging. 


To run the consumer test:
===

1. Delete the `test` topic on kafka

```shell
./kafka-topics.sh --zookeeper zookeeper-host:2181 --delete --topic test
```

2. Load data into a Kafka instance:

```shell
./kafka-producer-perf-test.sh --topic test --producer-props bootstrap.servers=kafka-host:9092 --throughput -1 --num-records 1000000000 --record-size 10
```


3. Create the baseline (repeat many times as required for obtaining stable and reproducible results, but at least 3):

```shell
mvn -Pcamel-3.11.2 clean package && ./start-consumer.sh kafka-host:9092 3.11.2 1000000000 1
```


4. Run the test on the snapshot code (repeat many times as required for obtaining stable and reproducible results, but at least 3 and equal to the amount of times ran on the baseline):

```shell
mvn -Pcamel-3.12.0-SNAPSHOT clean package && ./start-consumer.sh kafka-host:9092 3.12.0-SNAPSHOT 1000000000 1
```


Setup Automation
===

Edit the `development` inventory in ansible directory to use the hostname or IP of your test host. You should have a user with `sudo` permissions on the test host.

Then run: 

`make development`

