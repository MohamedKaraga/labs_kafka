package org.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

/**
 * Hello world!
 */
public class StreamApp {
    public static void main(String[] args) {
        final Properties properties = new Properties();
        final int CONGESTION_THRESHOLD = 300;
        final String topicIn = "vehicle-count";
        final String topicOut = "congestion-alerts";


        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "traffic-monitoring");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, );
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, );
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, );

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, String> vehicleCountStreams = streamsBuilder.stream();
        KStream<String, String> congestionAlertsStreams = vehicleCountStreams.filter((key, value) -> {
            int vehicleCount = Integer.parseInt(value);
           return vehicleCount > CONGESTION_THRESHOLD;
        });
        congestionAlertsStreams.foreach((key, value) -> System.out.println("ALERT Street : " + key + ", COUNT vehicle : " + value));
        congestionAlertsStreams.to();

        KafkaStreams kafkaStreams = new KafkaStreams();
        kafkaStreams.start();
    }
}