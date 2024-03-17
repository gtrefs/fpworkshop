package eu.javaland.fpworkshop.propertybasedtesting;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.*;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.shaded.com.google.common.base.Throwables;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class KafkaContainer extends org.testcontainers.containers.KafkaContainer {

    private static final String fullImageName = "confluentinc/cp-kafka:7.5.0";

//    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaContainer.class);
    private boolean createWebhooksTopicsUponStart = false;
    private int partitionsPerTopic = 8;
    public static final String[] topicNames = {};

    private final List<String> topics = new ArrayList<>();

    public KafkaContainer() {
        super(DockerImageName.parse(fullImageName).asCompatibleSubstituteFor("confluentinc/cp-kafka"));
        withEmbeddedZookeeper();
    }

//    public KafkaContainer createWebhooksKafkaTopicsUponStart() {
//        createWebhooksTopicsUponStart = true;
//        return (KafkaContainer) this.self();
//    }

//    public void clearAllTopics() {
//        try {
//            // bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic test
//            this.execInContainer("kafka-topics", "--bootstrap-server", "localhost:9092", "--delete", "--topic", String.join(",", topics));
//            var topicClearingResult = this
//                    .execInContainer("kafka-delete-records", "--bootstrap-server", "localhost:9092", "--offset-json-file", "/liveperson/configs/kafka_empty_all_topics.json");
//            LOGGER.info(topicClearingResult.toString());
//        } catch (IOException | InterruptedException e) {
//            Throwables.propagate(e);
//        }
//    }

//    /**
//     * Resets the Kafka offset in the private-webhooks-main topic to ignore conversations left over from 'dirty' test
//     * cases.
//     */
//    public void resetOffset() {
//        try {
//            var resetOffsetResult = this.
//                    execInContainer("kafka-consumer-groups", "--bootstrap-server", "localhost:9092", "--group", "webhooks-main-app", "--topic", "private-webhooks-main", "--reset-offsets", "--to-latest", "--execute");
//            LOGGER.info(resetOffsetResult.toString());
//
//        } catch (IOException | InterruptedException e) {
//            Throwables.propagate(e);
//        }
//    }

    public KafkaContainer partitionsPerTopic(int partitionsPerTopic) {
        this.partitionsPerTopic = partitionsPerTopic;
        return (KafkaContainer) this.self();
    }

    public KafkaContainer withClasspathResourceMapping(final String resourcePath, final String containerPath, final BindMode mode) {
        return (KafkaContainer) super.withClasspathResourceMapping(resourcePath, containerPath, mode);
    }

    private Map<String, KafkaProducer<byte[], String>> topicToProducer = new HashMap<>();

    public Future<RecordMetadata> send(String topic, String event) {
        final KafkaProducer<byte[], String> producer = reuseExistingProducerOrCreateNewOne(topic);
        return producer.send(new ProducerRecord<>(topic, event));
    }

    public KafkaProducer<byte[], String> reuseExistingProducerOrCreateNewOne(String topic) {
        if (topicToProducer.containsKey(topic)) {
            return topicToProducer.get(topic);
        }
        final KafkaProducer<byte[], String> producer = createStringProducer();
        topicToProducer.put(topic, producer);
        return producer;
    }

    protected KafkaProducer<byte[], String> createStringProducer() {
        return createProducer(new StringSerializer());
    }

    private <T> KafkaProducer<byte[], T> createProducer(Serializer<T> valueSerializer) {
        return new KafkaProducer<>(
            ImmutableMap.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers(),
                ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString()
            ),
            new ByteArraySerializer(),
            valueSerializer
        );
    }

    public String getConsumersByGroupId(String groupId) throws IOException, InterruptedException {
        Container.ExecResult consumersInfo = this
                .execInContainer("kafka-consumer-groups", "--describe", "--group",  groupId, "--bootstrap-server", "localhost:9092");

        return consumersInfo.getStdout();
    }

//    public void rebalanceGroup(String groupId) throws IOException, InterruptedException {
//        Container.ExecResult addConsumerToGroup = this
//                .execInContainer("sh", "-c", "kafka-console-consumer", "--bootstrap-server", "localhost:9092", "--group", groupId, "--topic", WH_MAIN_TOPIC_NAME, "&", "&& PID=$! && sleep 2 && kill $PID");
//
//        LOGGER.info("Triggered re-balance for group {}. Output {}", groupId, addConsumerToGroup.toString());
//    }

    public ConsumerGroup groups() {
        return new ConsumerGroup();
    }

    public List<String> getMessagesFromTopic(String topic) {
        return getMessagesFromTopic(topic, Duration.ofMinutes(5));
    }

    public List<String> getMessagesFromTopic(String topic, Duration waitForEventsDuration) {
        try (var consumer = createKafkaConsumerForTopic(topic)) {
//            var records = Stream.<ConsumerRecord<byte[], String>>empty();
//            do {
//                records = Stream.concat(records, pollRecords(consumer, waitForEventsDuration));
//            } while (hasMoreRecords(consumer));
//            return records.map(ConsumerRecord::value).toList();
            // will only poll 500 records at a time

            return StreamSupport.stream(consumer.poll(waitForEventsDuration).spliterator(), false)
                    .map(ConsumerRecord::value)
                    .collect(Collectors.toList());
        }
    }

    private boolean hasMoreRecords(KafkaConsumer<byte[], String> consumer) {
        return consumer.assignment().stream().map(consumer::currentLag).anyMatch(offset -> offset.orElse(0L) > 0L);
    }

    private Stream<ConsumerRecord<byte[], String>> pollRecords(KafkaConsumer<byte[], String> consumer, Duration waitForEventsDuration) {
        return StreamSupport.stream(consumer.poll(waitForEventsDuration).spliterator(), false);
    }

    public  KafkaConsumer<byte[], String> createKafkaConsumerForTopic(String topic) {
        final String bootstrapServers = getBootstrapServers();
        final KafkaConsumer<byte[], String> consumer = new KafkaConsumer<>(
                ImmutableMap.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"
                ), new ByteArrayDeserializer(), new StringDeserializer());
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        if (createWebhooksTopicsUponStart) {
            for (String topicName : topicNames) {
                createTopic(topicName);
            }
        }
    }

    public void createTopic(String topic) {
        try {
            this.execInContainer("kafka-topics", "--bootstrap-server", "localhost:9092", "--create", "--topic", topic, "--partitions", String.valueOf(partitionsPerTopic), "--replication-factor", "1");
        } catch (IOException | InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    public void deleteTopic(String topic) {
        try {
            var topicCreationResult = this
                    .execInContainer("kafka-topics", "--bootstrap-server", "localhost:9092", "--delete", "--topic", topic);
//            LOGGER.info(topicCreationResult.toString());
        } catch (IOException | InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    public List<TopicPartition> getWebhooksTopicPartitions(String topicName) {
        return IntStream.range(0, getPartitionsPerTopic())
                .mapToObj(i -> new TopicPartition(topicName, i))
                .collect(Collectors.toList());
    }
    public int getPartitionsPerTopic() {
        return partitionsPerTopic;
    }

    public class ConsumerGroup {

//        public void rebalance(String groupId) {
//            try {
//                KafkaContainer.this.rebalanceGroup(groupId);
//            } catch (IOException | InterruptedException e) {
//                Throwables.propagate(e);
//            }
//        }

        public List<Consumer> getConsumersFromGroup(String consumerGroup) {
            List<Consumer> consumers = new ArrayList<>();
            try {
                String kafkaConsumerGroup = KafkaContainer.this.getConsumersByGroupId(consumerGroup);
                String[] lines = kafkaConsumerGroup.split("\\n");
                List<String> consumerLines = Arrays.stream(lines)
                        .filter(line -> !line.isEmpty() && !line.contains("TOPIC"))
                        .collect(Collectors.toList());

//                LOGGER.info("===================================");
//                consumerLines.forEach(LOGGER::info);

                consumerLines.forEach(line -> {
                    String[] consumerInfo = line.split(" +");
                    consumers.add(new Consumer().setTopic(consumerInfo[1]).setPartition(consumerInfo[2])
                            .setCurrentOffset(consumerInfo[3])
                            .setLogEndOffset(consumerInfo[4]).setLag(consumerInfo[5]).setConsumerId(consumerInfo[6])
                            .setHost(consumerInfo[7]).setClientId(consumerInfo[8]).setConsumerGroup(consumerGroup));
                });
                return consumers;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return consumers;
        }

        public class Consumer {
            public String consumerId;
            public String topic;
            public String partition;
            public String currentOffset;
            public String logEndOffset;
            public String lag;
            public String host;
            public String clientId;
            public String consumerGroup;

            public void refresh() {
                try {
                    String kafkaConsumerGroup = KafkaContainer.this.getConsumersByGroupId(consumerGroup);
                    String[] lines = kafkaConsumerGroup.split("\\n");
                    Optional<String> consumerLine = Arrays.stream(lines)
                            .filter(line -> line.contains(consumerId)).findFirst();
                    consumerLine.ifPresent(c -> {
                        String[] infos = c.split(" +");
                        topic = infos[0];
                        partition = infos[1];
                        currentOffset = infos[2];
                        logEndOffset = infos[3];
                        lag = infos[4];
                    });
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public String getConsumerId() {
                return consumerId;
            }

            public Consumer setConsumerId(String consumerId) {
                this.consumerId = consumerId;
                return this;
            }

            public String getHost() {
                return host;
            }

            public Consumer setHost(String host) {
                this.host = host;
                return this;
            }

            public String getClientId() {
                return clientId;
            }

            public Consumer setClientId(String clientId) {
                this.clientId = clientId;
                return this;
            }

            public String getTopic() {
                return topic;
            }

            public Consumer setTopic(String topic) {
                this.topic = topic;
                return this;
            }

            public String getPartition() {
                return partition;
            }

            public Consumer setPartition(String partition) {
                this.partition = partition;
                return this;
            }

            public String getCurrentOffset() {
                return currentOffset;
            }

            public Consumer setCurrentOffset(String currentOffset) {
                this.currentOffset = currentOffset;
                return this;
            }

            public String getLogEndOffset() {
                return logEndOffset;
            }

            public Consumer setLogEndOffset(String logEndOffset) {
                this.logEndOffset = logEndOffset;
                return this;
            }

            public String getLag() {
                return lag;
            }

            public Consumer setLag(String lag) {
                this.lag = lag;
                return this;
            }

            public String getConsumerGroup() {
                return consumerGroup;
            }

            public Consumer setConsumerGroup(String consumerGroup) {
                this.consumerGroup = consumerGroup;
                return this;
            }

            @Override
            public String toString() {
                return "Consumer{" +
                        "consumerId='" + consumerId + '\'' +
                        ", topic='" + topic + '\'' +
                        ", partition='" + partition + '\'' +
                        ", currentOffset='" + currentOffset + '\'' +
                        ", logEndOffset='" + logEndOffset + '\'' +
                        ", lag='" + lag + '\'' +
                        ", host='" + host + '\'' +
                        ", clientId='" + clientId + '\'' +
                        ", consumerGroup='" + consumerGroup + '\'' +
                        '}';
            }
        }
    }
}
