### Interacting with kafka:

#### Official tools

Scripts. Usually in `/opt`.

```bash
./kafka-topics.sh --bootstrap-server kafka1:9092 --list
```

```bash
./kafka-topics.sh --bootstrap-server kafka1:9092 --create --topic example --replication-factor 3 --partitions 3 --config retention.ms=300000
```

```bash
./kafka-topics.sh --bootstrap-server kafka1:9092 --describe --topic example
```

```bash
./kafka-console-producer.sh --bootstrap-server kafka1:9092 --topic example --property parse.key=true --property key.separator=:
```

```bash
./kafka-console-consumer.sh --bootstrap-server kafka1:9092 --topic example \
--group test-group-1 \
--property print.key=true \
--property key.separator=: \
--property print.offset=true \
--property print.partition=true \
--from-beginning
```

Reading consumer offsets:

```bash
./kafka-console-consumer.sh --bootstrap-server kafka1:9092 --topic __consumer_offsets \
--property exclude.internal.topics=false \
--formatter 'kafka.coordinator.group.GroupMetadataManager$OffsetsMessageFormatter' \
--from-beginning
```

Show consumer groups

```bash
./kafka-consumer-groups.sh --bootstrap-server kafka1:9092 --list
```

Get offsets for consumer groups

```bash
./kafka-consumer-groups.sh --bootstrap-server kafka1:9092 \
--group one-by-one-condition-book-lines-consumer \
--describe
```

Reset offset for consumer groups

```bash
./kafka-consumer-groups.sh --bootstrap-server kafka1:9092 \
--group one-by-one-condition-book-lines-consumer \
--reset-offsets --all-topics --to-earliest --execute
```

#### kcat (kafkacat)

Describe cluster

```bash
kcat -L -b kafka1:9092
```


```bash
kcat -C -b kafka1:9092 -t book-lines -o beginning -J -e
```

```bash
kcat -C -b kafka1:9092 -t book-lines -o -2000 -J -e
```

#### Akhq setup

#### IDE kafka plugin

#### Important properties by SRP:

##### Consumer:

 - key.deserializer
 - value.deserializer
 - bootstrap.servers
 - fetch.min.bytes (guarded by fetch.max.wait.ms)
 - group.id
 - max.partition.fetch.bytes
 - allow.auto.create.topics
 - auto.offset.reset
 - enable.auto.commit
 - group.instance.id
 - max.poll.interval.ms
 - max.poll.records
 - partition.assignment.strategy
   - https://www.confluent.io/blog/incremental-cooperative-rebalancing-in-kafka/
   - https://www.conduktor.io/blog/kafka-partition-assignment-strategy/
   - https://kafka.apache.org/10/javadoc/org/apache/kafka/clients/consumer/StickyAssignor.html
 - client.id

##### Producer

 - key.serializer
 - value.serializer
 - bootstrap.servers
 - compression.type
 - retries
 - batch.size
 - client.id
 - linger.ms
 - acks
 - enable.idempotence

##### Topic
 - cleanup.policy
 - compression.type
 - max.message.bytes
 - retention.bytes
 - retention.ms
 - segment.bytes
 - segment.ms

##### Broker

docker-compose

 - broker.id
 - zookeeper.connect
 - advertised.listeners
 - auto.create.topics.enable
 - group.initial.rebalance.delay.ms
