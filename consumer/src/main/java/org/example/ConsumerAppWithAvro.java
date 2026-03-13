package org.example;

import com.example.avro.User;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Lab 06 - Kafka Consumer avec Avro et Schema Registry
 *
 * Ce consommateur est la version évoluée du Lab 04. Au lieu de désérialiser
 * de simples String, il reconstruit des objets Java typés (User) grâce au
 * schéma Avro enregistré dans le Schema Registry.
 *
 * La classe User a été générée automatiquement par le plugin Maven avro-maven-plugin
 * à partir du fichier user.avsc (voir étape 5 du lab).
 */
public class ConsumerAppWithAvro {
    public static void main(String[] args) {
        Properties props = new Properties();

        // Adresse du broker Kafka en mode local (sans Docker Compose ici)
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Identifiant du consumer group, Kafka suit les offsets par groupe
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-consumer-group");

        // La clé reste un String simple, on utilise donc le désérialiseur standard
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Objectif du lab : remplacer StringDeserializer par KafkaAvroDeserializer pour la valeur.
        // Ce désérialiseur contacte le Schema Registry pour récupérer le schéma
        // et reconstruire l'objet Java correspondant.
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

        // URL du Schema Registry : le désérialiseur l'interroge à chaque message
        // pour retrouver le schéma associé à l'identifiant encodé dans le message.
        props.put("schema.registry.url", "http://localhost:8081");

        // SPECIFIC_AVRO_READER à true indique au désérialiseur de reconstruire
        // une instance de la classe Java générée (User) plutôt qu'un objet GenericRecord générique.
        // Cela permet d'accéder directement aux champs via user.getName(), user.getAge(), etc.
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        // try-with-resources garantit la fermeture propre du consommateur
        // et déclenche un rebalance pour libérer les partitions assignées
        try (KafkaConsumer<String, User> kafkaConsumer = new KafkaConsumer<>(props)) {

            // Abonnement au topic "users" alimenté par le ProducerAppWithAvro du même lab
            kafkaConsumer.subscribe(Collections.singleton("users"));

            // Boucle de polling infinie, pattern classique d'un consommateur long-running
            while (true) {

                // poll() récupère les messages disponibles et maintient le consommateur
                // actif dans le groupe (évite un rebalance sur session.timeout.ms)
                ConsumerRecords<String, User> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, User> record : consumerRecords) {
                    // record.value() est maintenant un objet User typé, pas un String brut.
                    // toString() affiche les champs définis dans le schéma Avro (name, age).
                    System.out.println("Consumed message: (" + record.key() + ", " + record.value() + ")");
                }
            }
        }
    }
}