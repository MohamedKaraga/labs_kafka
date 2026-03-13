package org.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;

/**
 * Lab 03 - Kafka Producer IoT
 *
 * Ce producteur simule des capteurs IoT qui mesurent un nombre de véhicules.
 * Il publie un million de messages dans le topic "vehicle-count" en espacant
 * chaque envoi d'une seconde pour simuler un flux de données en temps réel.
 */
public class ProducerApp {
    public static void main(String[] args) throws InterruptedException {

        // Nombre total de messages à produire, simule un flux IoT de longue durée
        final int NUMBER_OF_RECORD = 1000000;

        // Bornes de la valeur aléatoire simulant un comptage de véhicules entre 1 et 500
        final int MIN = 1;
        final int MAX = 500;

        // Le topic cible, doit exister ou auto.create.topics.enable doit être true sur le broker
        final String topicName = "vehicle-count";

        // Objectif du lab : configurer le producteur (ProducerConfig)
        final Properties configs = new Properties();

        // Adresse du broker Kafka, on utilise le nom du service Docker (Lab 02)
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Sérialiseurs : Kafka ne transporte que des octets, il faut donc convertir
        // la clé et la valeur en bytes avant l'envoi. StringSerializer gère ce passage.
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Objectif du lab : tester linger.ms, batch.size et la compression

        // Exemple de configuration du batching (décommenter pour tester) :
        // configs.put(ProducerConfig.LINGER_MS_CONFIG, "20");
        // configs.put(ProducerConfig.BATCH_SIZE_CONFIG, "65536");

        // Exemple de configuration de la compression (décommenter pour tester) :
        // configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // try-with-resources garantit l'appel à kafkaProducer.close() en fin de bloc,
        // ce qui vide le buffer interne et attend l'accusé de réception des derniers messages
        try (KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(configs)) {

            for (int i = 0; i < NUMBER_OF_RECORD; i++) {

                // La clé identifie le capteur. Kafka utilise la clé pour choisir la partition :
                // tous les messages du même capteur atterrissent dans la même partition,
                // ce qui garantit l'ordre par capteur.
                String key = "sensor-" + i;

                // Valeur simulée : un entier aléatoire représentant un comptage de véhicules
                int value = new Random().nextInt(MAX - MIN) + MIN;

                // ProducerRecord encapsule le message : topic, clé et valeur.
                // La valeur est convertie en String car le sérialiseur attend un String.
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, key, String.valueOf(value));

                System.out.println("Produced message: (" + key + ", " + value + ")");

                // send() est asynchrone : le message est placé dans le buffer interne du producteur
                // et envoyé par un thread en arrière-plan. Le callback est appelé une fois
                // que le broker a accusé réception (ou en cas d'erreur).
                kafkaProducer.send(producerRecord, ProducerApp::callBack);

                // Pause d'une seconde entre chaque envoi pour simuler un flux IoT temps réel
                Thread.sleep(1000);
            }
        }
    }

    // Callback appelé après chaque accusé de réception du broker.
    // Si une exception est présente, l'envoi a échoué (erreur réseau, timeout, etc.).
    // Sinon, recordMetadata contient le topic, la partition et l'offset attribué au message.
    private static void callBack(RecordMetadata recordMetadata, Exception e) {
        if (e != null) {
            System.out.println("Error occurs : " + e.getMessage());
        } else {
            System.out.println("ack --> offset : " + recordMetadata.offset());
        }
    }
}