package eu.javaland.fpworkshop.propertybasedtesting;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.*;
import org.testcontainers.containers.BindMode;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Extension of the Kafka container offering convenience actions for Kafka.
 *
 * <p> Following actions are added:
 * <ul>
 *  <li>{@link #send(String, String)} send a message to given topic</li>
 *  <li>{@link #getMessagesFromTopic(String, Duration)} until wait time is reached try reading from the topic.</li>
 *  <li>{@link #createTopic(String)} creates a topic with 8 partitions.</li>
 *  <li>{@link #deleteTopic(String)} (String)} deletes given topic.</li>
 * <ul/>
 */
public class KafkaContainer extends org.testcontainers.containers.KafkaContainer {

    private static final String fullImageName = "confluentinc/cp-kafka:7.5.0";
    private int partitionsPerTopic = 8;

    public KafkaContainer() {
        super(DockerImageName.parse(fullImageName).asCompatibleSubstituteFor("confluentinc/cp-kafka"));
        withEmbeddedZookeeper();
    }

    public KafkaContainer withClasspathResourceMapping(final String resourcePath, final String containerPath, final BindMode mode) {
        return (KafkaContainer) super.withClasspathResourceMapping(resourcePath, containerPath, mode);
    }

    private final Map<String, KafkaProducer<byte[], String>> topicToProducer = new HashMap<>();

    public RecordMetadata sendAndAwait(String topic, String message) {
        try {
            return send(topic, message).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Future<RecordMetadata> send(String topic, String event) {
        final KafkaProducer<byte[], String> producer = reuseExistingProducerOrCreateNewOne(topic);
        return producer.send(new ProducerRecord<>(topic, event));
    }

    private KafkaProducer<byte[], String> reuseExistingProducerOrCreateNewOne(String topic) {
        if (topicToProducer.containsKey(topic)) {
            return topicToProducer.get(topic);
        }
        final KafkaProducer<byte[], String> producer = createStringProducer();
        topicToProducer.put(topic, producer);
        return producer;
    }

    private KafkaProducer<byte[], String> createStringProducer() {
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

    public ExecResult createTopic(String topic) {
        try {
            return this.execInContainer("kafka-topics", "--bootstrap-server", "localhost:9092", "--create", "--topic", topic, "--partitions", String.valueOf(partitionsPerTopic), "--replication-factor", "1");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ExecResult deleteTopic(String topic) {
        try {
            return execInContainer("kafka-topics", "--bootstrap-server", "localhost:9092", "--delete", "--topic", topic);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
