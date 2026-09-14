package com.pluralsight.kafka.consumer;


import com.pluralsight.kafka.model.Product;
import com.pluralsight.kafka.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.util.ClassSecurityValidator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Properties;

import static java.util.Arrays.asList;

@Slf4j
public class Main {
    private static final String TOPIC = "user-tracking-avro";

    static {
        ClassSecurityValidator.setGlobal(clazz ->
                clazz != null && clazz.getName().startsWith("com.pluralsight.kafka.model.")
        );
    }
    static void main(String[] args) {
        SuggestionEngine suggestionEngine = new SuggestionEngine();

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "user-tracking-consumer");

        props.put("auto.offset.reset","earliest");// so will read from the start of the topic every time
        //props.put("auto.offset.reset","latest");// default, we will read only the last records added

        props.put("key.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("specific.avro.reader", "true");
        props.put("schema.registry.url", "http://localhost:8081");


        try(KafkaConsumer<User, Product> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(asList(TOPIC));

            while (true) {
                ConsumerRecords<User, Product> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<User, Product> cRecord : records) {
                    suggestionEngine.processSuggestions(cRecord.key(), cRecord.value());
                }
            }
        } catch (Exception e) {
            log.error(String.format("An exception was raised whilst trying to consume from %s", TOPIC), e);
            throw new RuntimeException(e);
        }
    }
}
