package eu.javaland.fpworkshop.propertybasedtesting;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.lifecycle.BeforeTry;
import net.jqwik.api.statistics.Statistics;
import net.jqwik.testcontainers.Container;
import net.jqwik.testcontainers.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class KafkaProperties {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer();

    List<String> appendOnlyQueueAsModel = new ArrayList<>();

    static AtomicInteger topicCounter = new AtomicInteger();

    final String topicName = "messages_";


    @Property(tries = 10)
    public void readWhatYouWrite(@ForAll List<@AlphaChars String> messages){
        messages.forEach(message -> kafkaContainer.sendAndAwait(currentTopicName(), message));
        appendOnlyQueueAsModel.addAll(messages);

        var fromTopic = kafkaContainer.getMessagesFromTopic(currentTopicName(), Duration.ofSeconds(10));

        Statistics.label("Message size").collect(messages.size());
        Statistics.label("Messages from Kafka").collect(fromTopic.size());
        Statistics.label("Messages from Map").collect(appendOnlyQueueAsModel.size());
        assertThat(fromTopic).hasSameElementsAs(appendOnlyQueueAsModel);
    }


    @Property(tries = 10)
    public void readWhatYouWriteWithLargeInputs(@ForAll @Size(min = 500, max = 550)  List<@AlphaChars String> messages){
        messages.forEach(message -> kafkaContainer.sendAndAwait(currentTopicName(), message));
        appendOnlyQueueAsModel.addAll(messages);

        var fromTopic = kafkaContainer.getMessagesFromTopic(currentTopicName(), Duration.ofSeconds(10));

        Statistics.label("Message size").collect(messages.size());
        Statistics.label("Messages from Kafka").collect(fromTopic.size());
        Statistics.label("Messages from Map").collect(appendOnlyQueueAsModel.size());
        assertThat(fromTopic).hasSameElementsAs(appendOnlyQueueAsModel);
    }

    @BeforeTry
    public void reset(){
        kafkaContainer.deleteTopic(currentTopicName());
        String topic = nextTopicName();
        System.out.println("New topic: "+topic);
        kafkaContainer.createTopic(topic);
        appendOnlyQueueAsModel.clear();
    }

    String nextTopicName(){
        return topicName + topicCounter.incrementAndGet();
    }

    String currentTopicName() {
        return topicName + topicCounter.get();
    }
}
