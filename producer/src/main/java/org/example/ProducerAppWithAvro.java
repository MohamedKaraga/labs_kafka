package org.example;

import com.example.avro.User;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Hello world!
 */


public class ProducerAppWithAvro {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");

        KafkaProducer<String, User> producer = new KafkaProducer<>(props);

        User user = new User("John Doe", 30);

        ProducerRecord<String, User> record = new ProducerRecord<>("users", user.getName().toString(), user);
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    exception.printStackTrace();
                } else {
                    System.out.println("Record sent successfully to " + metadata.topic() +
                            " partition " + metadata.partition() +
                            " with offset " + metadata.offset());
                }
            }
        });

        // Il est important de fermer le producteur après l'envoi des messages
        producer.close();
    }
}
