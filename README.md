Camel Load Tester
=========================

Introduction
===

This project contains a collection of load testers for Apache Camel. This is meant as a macro-benchmark tool to provide additional
data for the [camel-performance-tests](http://github.com/apache/camel-performance-tests) project. This is meant as a tool for Camel
developers and contributors to evaluate internal changes to Camel. This is not meant as a user tool.

Running Tests
====

Running Producer Tests
=====

```shell
java -Doutput.dir=/path/to/report \                     # Path to the report directory
    -Dtest.rate.file=/path/to/report/report.data        # Path to the report file
    -Dtest.name=threaded-disruptor-producer \           # Test name to add to the reports
    -XX:+UseNUMA -Xmx4G -Xms4G -server \                # Common Java options (tweak as needed)
    -Dcamel.main.durationMaxSeconds=180 \               # Test duration (in seconds)
    -Dcamel.main.shutdownTimeout=5 \                    # Shutdown timeout (in seconds)
    -Dcamel.main.durationMaxIdleSeconds=10 \            # Max idle time (in seconds)
    -Dtest.producer.type=threaded-disruptor-producer \  # Test producer type (see the next section for details)
    -Dtest.thread.count=4 \                             # Consumer count
    -Dtest.thread.count.producer=4 \                    # Producer count
    -Dtest.on.complete.action=exit                      # Exit action
```

Test Types
=====

* dataset-batched-processor: tests the dataset producer with an aggregation EIP
* dataset-injection-to-direct: tests dataset, with JSon unmarshalling, sending to direct
* dataset-injection-to-seda: tests dataset, with JSon unmarshalling, sending to SEDA
* dataset-noop-to-direct: simple dataset to direct route
* dataset-noop-to-seda: simple dataset to SEDA route
* dataset-threaded-processor: dataset with threaded processor 
* kafka: from dataset to Kafka
* threaded-seda-producer: a highly threaded SEDA producer
* threaded-disruptor-producer: a highly threaded Disruptor producer

Running Kafka-based Tests
====


To run the consumer test:
====

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
====

Edit the `development` inventory in ansible directory to use the hostname or IP of your test host. You should have a user with `sudo` permissions on the test host.

Then run: 

`make development`


```
podman run --rm --name influxdb -v influxdb-data:/root/.influxdbv2 -p 8086:8086 quay.io/influxdb/influxdb:v2.0.9
```
