package org.example;

import com.example.avro.User;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Lab 06 - Kafka Producer avec Avro et Schema Registry
 *
 * Ce producteur est la version évoluée du Lab 03. Au lieu d'envoyer des String bruts,
 * il sérialise des objets Java typés (User) au format Avro et s'appuie sur le Schema Registry
 * pour centraliser et versionner le schéma utilisé.
 *
 * La classe User a été générée automatiquement par le plugin Maven avro-maven-plugin
 * à partir du fichier user.avsc (voir étape 5 du lab).
 */
public class ProducerAppWithAvro {
    public static void main(String[] args) {
        Properties props = new Properties();

        // Adresse du broker Kafka en mode local (sans Docker Compose ici)
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // La clé reste un String simple, on conserve donc le sérialiseur standard
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Objectif du lab : remplacer StringSerializer par KafkaAvroSerializer pour la valeur.
        // Ce sérialiseur convertit l'objet User en bytes au format Avro avant l'envoi,
        // et enregistre automatiquement le schéma dans le Schema Registry si besoin.
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        // URL du Schema Registry : le sérialiseur y enregistre le schéma Avro au premier envoi
        // et y associe un identifiant numérique qui sera encodé dans chaque message.
        // Le consommateur utilisera cet identifiant pour retrouver le schéma et désérialiser.
        props.put("schema.registry.url", "http://localhost:8081");

        // Ici le producteur n'est pas dans un try-with-resources, producer.close() est
        // donc appelé manuellement plus bas. Les deux approches sont valides.
        KafkaProducer<String, User> producer = new KafkaProducer<>(props);

        // Création d'un objet User à partir de la classe générée par le plugin Avro.
        // Les champs correspondent exactement à ceux définis dans user.avsc (name, age).
        User user = new User("John Doe", 30);

        // ProducerRecord encapsule le message : topic cible, clé et valeur typée.
        // La clé est le nom de l'utilisateur, ce qui garantit que tous les messages
        // d'un même utilisateur atterrissent dans la même partition (ordre garanti).
        ProducerRecord<String, User> record = new ProducerRecord<>("users", user.getName().toString(), user);

        // send() est asynchrone. Le callback ci-dessous est appelé une fois que le broker
        // a accusé réception du message, ou en cas d'erreur lors de l'envoi.
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    // Une exception ici indique un échec définitif après les tentatives de retry
                    exception.printStackTrace();
                } else {
                    // L'accusé de réception confirme le topic, la partition et l'offset attribué
                    System.out.println("Record sent successfully to " + metadata.topic() +
                            " partition " + metadata.partition() +
                            " with offset " + metadata.offset());
                }
            }
        });

        // close() est indispensable : il vide le buffer interne du producteur (flush)
        // et attend l'accusé de réception de tous les messages encore en transit
        // avant de libérer les ressources réseau.
        producer.close();
    }
}