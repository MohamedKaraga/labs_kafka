package org.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

/**
 * Lab 08 - Kafka Streams : détection de congestion en temps réel
 *
 * Cette application consomme le flux de comptages de véhicules produit par le Lab 03,
 * filtre les valeurs dépassant un seuil de congestion, affiche les alertes en console
 * et publie les messages filtrés dans un topic dédié.
 *
 * Contrairement à un consumer classique, Kafka Streams gère lui-même le polling,
 * le commit des offsets et la topologie de traitement de manière déclarative.
 */
public class StreamApp {
    public static void main(String[] args) {

        final Properties properties = new Properties();

        // Seuil au-delà duquel un comptage de véhicules est considéré comme une congestion
        final int CONGESTION_THRESHOLD = 300;

        // Topic source : flux de comptages produit par le ProducerApp du Lab 03
        final String topicIn = "vehicle-count";

        // Topic de sortie : contiendra uniquement les messages dépassant le seuil
        final String topicOut = "congestion-alerts";

        // Objectif du lab : configurer l'application Streams (StreamsConfig)

        // Identifiant unique de l'application Streams. Kafka l'utilise pour regrouper
        // les instances, suivre les offsets et nommer les topics internes de l'application.
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "traffic-monitoring");

        // Adresse du broker Kafka en mode local
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Serdes (Serializer + Deserializer) par défaut pour les clés et valeurs.
        // StringSerde est cohérent avec les String produits par le ProducerApp du Lab 03.
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        // Objectif du lab : construire la topologie avec StreamsBuilder

        // StreamsBuilder est le point d'entrée pour décrire le graphe de traitement (topologie).
        // Chaque appel (stream, filter, to...) ajoute un noeud au graphe sans encore l'exécuter.
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // Création du KStream à partir du topic d'entrée.
        // Un KStream représente un flux continu et non borné d'enregistrements.
        KStream<String, String> vehicleCountStreams = streamsBuilder.stream(topicIn);

        // Objectif du lab : appliquer une transformation sur le flux

        // filter() ne conserve que les messages dont le comptage dépasse le seuil.
        // Le prédicat est évalué pour chaque message individuellement, sans état ni fenêtre.
        KStream<String, String> congestionAlertsStreams = vehicleCountStreams.filter((key, value) -> {
            int vehicleCount = Integer.parseInt(value);
            return vehicleCount > CONGESTION_THRESHOLD;
        });

        // foreach() est une opération terminale : elle consomme chaque message filtré
        // pour l'afficher en console. Elle ne produit rien dans le flux en aval.
        congestionAlertsStreams.foreach((key, value) ->
                System.out.println("ALERT Street : " + key + ", COUNT vehicle : " + value));

        // to() écrit les messages filtrés dans le topic de sortie.
        // C'est ici que les alertes deviennent disponibles pour d'autres consommateurs.
        congestionAlertsStreams.to(topicOut);

        // Objectif du lab : démarrer l'application KafkaStreams

        // build() compile la topologie décrite par le StreamsBuilder en un objet Topology immuable.
        // KafkaStreams prend ce graphe et les propriétés pour instancier l'application.
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);

        // start() lance les threads internes de Kafka Streams : polling, traitement et commit.
        // L'appel est non bloquant, l'application tourne en arrière-plan jusqu'à kafkaStreams.close().
        kafkaStreams.start();
    }
}