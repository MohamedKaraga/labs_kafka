package org.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.Random;

/**
 * Hello world!
 */


public class ProducerApp {
    public static void main(String[] args) {
        final int NUMBER_OF_RECORD = 1000000;
        final int MIN = 1;
        final int MAX = 500;
        final String topicName = "vehicle-count";

        final Properties configs = new Properties();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, );
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, );
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, );

        try (KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>()) {
            for (int i = 0; i < NUMBER_OF_RECORD; i++) {
                String key = "sensor-" + i;
                int value = new Random().nextInt(MAX - MIN) + MIN;;

                ProducerRecord<String, String> producerRecord = new ProducerRecord<>();
                System.out.println("Produced message: (" + key + ", " + value + ")");
                kafkaProducer.send();
            }
        }
    }

    private static void callBack(RecordMetadata recordMetadata, Exception e) {
        if (e != null) {
            System.out.println("Error occurs : " + e.getMessage());
        } else {
            System.out.println("ack : " + recordMetadata);
        }
    }
}
