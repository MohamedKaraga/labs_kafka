package org.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Lab 04 - Kafka Consumer
 *
 * Ce consommateur lit les messages publiés par le producteur IoT (Lab 03).
 * Il appartient à un consumer group, ce qui permet à Kafka de distribuer
 * les partitions entre plusieurs instances si nécessaire (scalabilité horizontale).
 */
public class ConsumerApp {
    public static void main(String[] args) {

        // Le topic à consommer, doit correspondre au topic écrit par le producteur (Lab 03)
        String topicName = "vehicle-count";

        // Identifiant du consumer group : Kafka l'utilise pour suivre les offsets commités.
        // Toutes les instances partageant le même group ID forment un seul consommateur logique.
        // Si deux instances tournent avec le même group ID, Kafka répartit les partitions entre elles.
        String groupId = "iot-consumer-group";

        // Objectif du lab : configurer le consommateur (ConsumerConfig)
        Properties configs = new Properties();

        // Adresse du broker Kafka, on utilise le nom du service Docker (Lab 02)
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Désérialiseurs : doivent correspondre aux sérialiseurs utilisés par le producteur.
        // Le producteur a envoyé des clés et valeurs en String, on désérialise donc en String.
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Rattache ce consommateur à un groupe afin que Kafka gère le suivi des offsets par groupe
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Objectif du lab : tester les configurations de performance

        // FETCH_MAX_BYTES : quantité maximale de données que le broker retourne par requête fetch
        // (toutes partitions confondues). Ici ~5 Mo pour réduire les allers-retours réseau
        // lorsque le topic est fortement chargé.
        configs.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "5000000");

        // FETCH_MAX_WAIT_MS : si le broker n'a pas encore FETCH_MIN_BYTES de données disponibles,
        // il attend au maximum cette durée avant de répondre quand même.
        // 5000ms signifie que le broker attend jusqu'à 5 secondes pour regrouper les réponses,
        // ce qui réduit la charge CPU au prix d'une latence plus élevée.
        configs.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "5000");

        // try-with-resources garantit l'appel automatique de kafkaConsumer.close(),
        // ce qui déclenche un rebalance propre pour que d'autres consommateurs
        // puissent reprendre les partitions libérées.
        try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(configs)) {

            // Abonnement au topic. Kafka assignera une ou plusieurs partitions à cette instance.
            // On utilise un singleton car on ne consomme qu'un seul topic ici.
            kafkaConsumer.subscribe(Collections.singleton(topicName));

            // Boucle de polling infinie, pattern classique d'un consommateur long-running
            while (true) {

                // poll() récupère les messages disponibles et maintient le consommateur actif dans le groupe.
                // Le paramètre Duration est le temps maximum à bloquer si aucun message n'est disponible.
                // Ne pas appeler poll() avant session.timeout.ms déclenche un rebalance.
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));

                // On itère sur chaque message du batch retourné par ce cycle de poll
                for (ConsumerRecord<String, String> record : consumerRecords) {
                    // record.key()   : clé de routage vers la partition, définie par le producteur
                    // record.value() : la donnée capteur (JSON envoyé par le producteur du Lab 03)
                    System.out.println("Consumed message: (" + record.key() + ", " + record.value() + ")");
                }

                // Note : avec enable.auto.commit=true (valeur par défaut), les offsets sont commités
                // automatiquement en arrière-plan. Voir la section commit manuel du Lab 04 pour une
                // alternative offrant des garanties de traitement "exactly-once".
            }
        }
    }
}