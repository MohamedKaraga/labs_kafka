# Labs Kafka - Formation Developer

## Lab 01 : Installation Sans Docker - 45min

### 🎯 Objectifs
- Configurer Kafka en mode KRaft
- Créer et tester des topics
- Produire et consommer des messages

### 📋 Prérequis
- JDK 17+ installé
- Droits administrateur

### 🛠️ Instructions

#### 1. Télécharger et installer Kafka
```bash
# Télécharger Kafka
wget https://downloads.apache.org/kafka/4.0.0/kafka_2.13-4.0.0.tgz

# Extraire
tar -xzf kafka_2.13-4.0.0.tgz
cd kafka_2.13-4.0.0

# Vérifier Java (doit afficher version 17+)
java -version
```

#### 2. Créer les répertoires et configurer
```bash
# Créer les dossiers de données
sudo mkdir -p /var/lib/kafka/data
sudo mkdir -p /var/lib/kafka/meta
sudo chown -R $(whoami):$(whoami) /var/lib/kafka
```

```bash
# Créer la configuration dans config/server.properties
cat > config/server.properties << EOF
# Configuration KRaft
process.roles=broker,controller
node.id=1
controller.quorum.voters=1@localhost:9093

# Listeners
listeners=PLAINTEXT://localhost:9092,CONTROLLER://localhost:9093
controller.listener.names=CONTROLLER
listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT

# Répertoires
log.dirs=/var/lib/kafka/data
metadata.log.dir=/var/lib/kafka/meta

# Options
auto.create.topics.enable=true
EOF
```

#### 3. Formater et démarrer Kafka
```bash
# Générer un UUID et formater
bin/kafka-storage.sh format -t $(bin/kafka-storage.sh random-uuid) -c config/server.properties

# Démarrer Kafka
bin/kafka-server-start.sh config/server.properties
```

#### 4. Tester dans de nouveaux terminaux
```bash
# Terminal 2 : Créer un topic
bin/kafka-topics.sh --create --topic test --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092

# Décrire le topic
bin/kafka-topics.sh --describe --topic test --bootstrap-server localhost:9092

# Terminal 3 : Producer
bin/kafka-console-producer.sh --topic test --bootstrap-server localhost:9092

# Terminal 4 : Consumer
bin/kafka-console-consumer.sh --topic test --from-beginning --bootstrap-server localhost:9092
```

### ✅ Validation
- [ ] Kafka démarre sans erreur dans le terminal 1
- [ ] Topic `test` créé avec succès
- [ ] Messages tapés dans le producer apparaissent dans le consumer

### 🔧 En cas de problème
```bash
# Si erreur de permissions
sudo chown -R $(whoami):$(whoami) /var/lib/kafka

# Si port occupé, tuer les processus Kafka
ps aux | grep kafka | awk '{print $2}' | xargs kill -9
```

---

## Lab 02 : Kafka avec Docker - 30min

### 🎯 Objectifs
- Déployer Kafka avec Docker
- Utiliser Control Center pour le monitoring
- Comprendre l'architecture conteneurisée

### 📋 Prérequis
- Docker et Docker Compose installés
- 8GB RAM disponible

### 🛠️ Instructions

#### 1. Récupérer le fichier Docker Compose
```bash
# Télécharger la configuration
wget https://raw.githubusercontent.com/MohamedKaraga/labs_kafka/refs/heads/master/docker-compose.yml

# Vérifier que Docker fonctionne
docker ps
```

#### 2. Démarrer les services
```bash
# Démarrer Kafka et Control Center
docker-compose up -d broker control-center

# Vérifier le démarrage (attendre ~30 secondes)
docker-compose ps
```

#### 3. Tester Kafka
```bash
# Entrer dans le conteneur broker
docker-compose exec broker /bin/bash

# Dans le conteneur : créer un topic
kafka-topics --bootstrap-server broker:9092 --create --topic test --partitions 1 --replication-factor 1

# Lister les topics
kafka-topics --bootstrap-server broker:9092 --list
```

#### 4. Test Producer/Consumer
```bash
# Terminal 1 : Producer (dans le conteneur)
kafka-console-producer --bootstrap-server broker:9092 --topic test

# Terminal 2 : Consumer (nouveau terminal, entrer dans le conteneur)
docker-compose exec broker /bin/bash
kafka-console-consumer --bootstrap-server broker:9092 --from-beginning --topic test
```

### ✅ Validation
- [ ] Conteneurs `broker` et `control-center` en statut "Up"
- [ ] Control Center accessible sur http://localhost:9021
- [ ] Messages échangés entre producer et consumer

### 🔧 En cas de problème
```bash
# Redémarrer proprement
docker-compose down -v
docker-compose up -d broker control-center

# Voir les logs en cas d'erreur
docker-compose logs broker
```

## Lab 03 : Producer {collapsible="true"}

* Simuler des producteurs Kafka intégré dans des appareils IoT servant à collecter les données des capteurs.
* Configurer le producteur pour publier les données des capteurs dans un topic Kafka.
* Configurer le batching, la compression pour optimiser la transmission des données.

1. Exécutez la commande suivante dans le terminal pour cloner le projet :

   ```bash
   git clone https://github.com/MohamedKaraga/labs_kafka.git
   ```

2. Allez dans le module producteur
   ```bash
   cd labs_kafka/producer
   ```
3. completez le code pour `ProducerConfig`, `ProducerRecord`, and `KafkaProducer`.
4. Démarrez le cluster Kafka avec l'option -d pour l'exécuter en mode détaché:
   ```bash
   docker-compose up -d broker control-center
   ```
5. Construire et exécuter le producteur
6. Exécutez le producteur avec différentes configurations pour `linger.ms` et `batch.size` afin d'observer leurs effets sur le batching des messages et le temps d'envoi.
7. Configurer le type de compression pour optimiser la transmission des données(`snappy` ou `gzip` ou `lz4`)
8. Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes

   ```bash
   docker-compose down -v
   ```
   
## Lab 04 : Consumer {collapsible="true"}

* Configurer le consommateur Kafka pour ingérer les données des capteurs depuis un topic Kafka en temps réel.
* Traiter les données entrantes
* Vous apprendrez comment ajuster des paramètres tels que `fetch.min.bytes`, `fetch.max.wait.ms`, `max.poll.records` pour obtenir des performances optimales.

1. Exécutez la commande suivante dans le terminal pour cloner le projet (cette étape n'est pas nécessaire si vous l'avez déjà faite dans le lab précédent):

   ```bash
   git clone https://github.com/MohamedKaraga/labs_kafka.git
   ```

2. Allez dans le module consommateur
   ```bash
   cd labs_kafka/consumer
   ```
3. completez le code pour `ConsumerConfig`, `KafkaConsumer`.
4. Démarrez le cluster Kafka avec l'option -d pour l'exécuter en mode détaché:
   ```bash
   docker-compose up -d broker control-center
   ```
5. Construire et exécuter le consommateur
6. Relancez le producer utilisé dans le `Lab 03` pour alimenter le topic 

7. Exécutez le consommateur avec les differentes configurations pour `fetch.min.bytes (5000000)`, `fetch.max.wait.ms (5000)` pour observer leurs effets sur la consommation des messages et les performances

8. Conversion d'un Consommateur Kafka avec Commit Automatique vers Commit Manuel

Vous disposez actuellement d'un consommateur Kafka qui utilise le mécanisme de commit automatique des offsets. Cependant, votre équipe a identifié des problèmes potentiels de fiabilité dans le pipeline de données lors d'incidents système. Pour améliorer la robustesse de votre application, vous devez implémenter un mécanisme de commit manuel.

Les objectifs sont : 

- Comprendre les différences entre commit automatique et manuel dans Kafka
- Implémenter une stratégie de commit manuel qui garantit le traitement "exactement une fois" (exactly-once)
- Tester la résistance de votre solution aux pannes

9. Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes

   ```bash
   docker-compose down -v
   ```

## Lab 05 : Kafka REST Proxy {collapsible="true"}

1. Démarrez le cluster Kafka avec l'option -d pour l'exécuter en mode détaché:
   ```bash
   docker-compose up -d broker control-center rest-proxy
   ```
2. Produire des messages dans un topic Kafka en utilisant le REST Proxy

   * Exécuter dans la console du conteneur `broker`
      ```bash
      docker-compose exec broker /bin/bash
      ```
   * Créer un topic kafka
   ```bash
   kafka-topics --create --topic bar --bootstrap-server broker:9092 --partitions 1 --replication-factor 1
   ```
   * Produire un message JSON dans le topic
   ```bash
   curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
   --data '{"records":[{"value":{"name":"toto", "age":30}}]}' \
   http://localhost:8082/topics/bar
   ```
3. Consommer des messages dans un topic Kafka en utilisant le REST Proxy
   * Créer une nouvelle instance consumer
   ```bash
   curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
   --data '{"name": "my_consumer_instance", "format": "json", "auto.offset.reset": "earliest"}' \
   http://localhost:8082/consumers/my_consumer_group
   ```   
   * Abonner le consommateur au topic
   ```bash
   curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
   --data '{"topics":["bar"]}' \
   http://localhost:8082/consumers/my_consumer_group/instances/my_consumer_instance/subscription
   ```   
   * Consommer des messages
   ```bash
   curl -X GET -H "Accept: application/vnd.kafka.json.v2+json" \
   http://localhost:8082/consumers/my_consumer_group/instances/my_consumer_instance/records
   ```
5. Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes

   ```bash
   docker-compose down -v
   ```

## Lab 06 : Schema Registry {collapsible="true"}

1. Démarrez le cluster Kafka avec l'option -d pour l'exécuter en mode détaché:

   ```bash
   docker-compose up -d broker control-center schema-registry
   ```
2. Enregistrer le schéma

   ```bash
   curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
   --data '{"schema": "{\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null}]}"}' \
   http://localhost:8081/subjects/user-value/versions
   ```
3. Produire et consommer des messages en utilisant le schéma avro

   * Transformez votre producer **Lab 03** et consumer **Lab 04** en utilisant le schéma et l'API schema registry.
   * Ajouter le repository maven de confluent
   ```xml
    <repositories>
    <repository>
      <id>confluent</id>
      <url>https://packages.confluent.io/maven/</url>
    </repository>
   </repositories>     
   ```
   * Ajouter les dependances Maven Avro et Schema Registry

        ```xml
       <!-- Avro dependencies -->
       <dependency>
           <groupId>org.apache.avro</groupId>
           <artifactId>avro</artifactId>
           <version>1.11.1</version>
       </dependency>
       <!-- Schema Registry dependencies -->
       <dependency>
          <groupId>io.confluent</groupId>
          <artifactId>kafka-avro-serializer</artifactId>
          <version>7.6.0</version>
       </dependency>
      ```

   * Créer le repertoire `src/main/avro` et definir le schéma Avro (user.avsc)

     ```json
        {
       "type": "record",
       "name": "User",
       "namespace": "com.example.avro",
       "fields": [
         {"name": "name", "type": "string"},
         {"name": "age", "type": "int"}
       ]
       }
     ```

   * Ajoutez le plugin Avro Maven à votre `pom.xml` du producteur et du consommateur pour générer des classes Java à partir du schéma Avro

     ```xml
     <build>
      <plugins>
          <plugin>
              <groupId>org.apache.avro</groupId>
              <artifactId>avro-maven-plugin</artifactId>
              <version>1.11.1</version>
              <executions>
                  <execution>
                      <phase>generate-sources</phase>
                      <goals>
                          <goal>schema</goal>
                      </goals>
                      <configuration>
                          <sourceDirectory>${project.basedir}/src/main/avro</sourceDirectory>
                          <outputDirectory>${project.basedir}/src/main/java</outputDirectory>
                      </configuration>
                  </execution>
              </executions>
          </plugin>
      </plugins>
     </build>
     ```
   
   * Exécutez la commande Maven pour générer les classes Java

        ```bash
        mvn clean compile
        ```
  
   * Complétez et exécutez pour produire avec le schéma Avro
   
     ```Java
          Properties props = new Properties();
          props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
          props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
          props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
          props.put("schema.registry.url", "http://localhost:8081");

          KafkaProducer<String, User> producer = new KafkaProducer<>(props);

          User user = new User("John Doe", 30);

          ProducerRecord<String, User> record = new ProducerRecord<>("users", user.getName().toString(), user);
     ```
     
   * Exécutez la classe Producer et assurez-vous qu'un message est produit dans le topic `users`
  
4. Consommer des messages en utilisant le schéma avro

   * Transformez votre consommateur `Lab 04` en utilisant le schéma et l'API du Schema Registry.
   * Ajoutez les dépendances Maven Avro et Schema Registry comme précédemment.
   * Ajoutez le plugin Avro Maven à votre `pom.xml` pour générer des classes Java à partir du schéma Avro comme précédemment.

     ```Java
           Properties props = new Properties();
           props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
           props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-consumer-group");
           props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
           props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
           props.put("schema.registry.url", "http://localhost:8081");
           props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

           KafkaConsumer<String, User> consumer = new KafkaConsumer<>(props);
     ```
     
   * Exécutez la classe Consumer et vérifiez que le consommateur lit le message du topic `users`.
     
5. Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes

   ```bash
   docker-compose down -v
   ```


## Lab 07 : Deployer un kafka connect {collapsible="true"}


1. Démarrez le cluster Kafka avec l'option -d pour l'exécuter en mode détaché:

   ```bash
   docker-compose up -d broker control-center postgres connect mongodb
   ```
   
2. Installer un connecteur JDBC

   ```bash
   docker-compose exec -u root connect confluent-hub install confluentinc/kafka-connect-jdbc:10.7.6
   ```
   
   **Output**
   ```Console
   The component can be installed in any of the following Confluent Platform installations: 
   1. / (installed rpm/deb package)
   2. / (where this tool is installed)
      Choose one of these to continue the installation (1-2): 1
      Do you want to install this into /usr/share/confluent-hub-components? (yN) N
   
   Specify installation directory: /usr/share/java/kafka
   
   Component's license:
   Confluent Community License
   https://www.confluent.io/confluent-community-license
   I agree to the software license agreement (yN) y
   
   Downloading component Kafka Connect JDBC 10.7.6, provided by Confluent, Inc. from Confluent Hub and installing into /usr/share/java/kafka
   Detected Worker's configs:
   1. Standard: /etc/kafka/connect-distributed.properties
   2. Standard: /etc/kafka/connect-standalone.properties
   3. Standard: /etc/schema-registry/connect-avro-distributed.properties
   4. Standard: /etc/schema-registry/connect-avro-standalone.properties
   5. Used by Connect process with PID : /etc/kafka-connect/kafka-connect.properties
      Do you want to update all detected configs? (yN) y
   
   Adding installation directory to plugin path in the following files:
   /etc/kafka/connect-distributed.properties
   /etc/kafka/connect-standalone.properties
   /etc/schema-registry/connect-avro-distributed.properties
   /etc/schema-registry/connect-avro-standalone.properties
   /etc/kafka-connect/kafka-connect.properties
   
   Completed

   ```
   
3. Installer un connecteur MongoDB

   ```bash
   docker-compose exec -u root connect confluent-hub install mongodb/kafka-connect-mongodb:latest
   ```
   
   **Output**
   ```Console
   The component can be installed in any of the following Confluent Platform installations: 
   1. / (installed rpm/deb package)
   2. / (where this tool is installed)
      Choose one of these to continue the installation (1-2): 1
      Do you want to install this into /usr/share/confluent-hub-components? (yN) N
   
   Specify installation directory: /usr/share/java/kafka
   
   Component's license:
   Confluent Community License
   https://www.confluent.io/confluent-community-license
   I agree to the software license agreement (yN) y
   
   Downloading component Kafka Connect JDBC 10.7.6, provided by Confluent, Inc. from Confluent Hub and installing into /usr/share/java/kafka
   Detected Worker's configs:
   1. Standard: /etc/kafka/connect-distributed.properties
   2. Standard: /etc/kafka/connect-standalone.properties
   3. Standard: /etc/schema-registry/connect-avro-distributed.properties
   4. Standard: /etc/schema-registry/connect-avro-standalone.properties
   5. Used by Connect process with PID : /etc/kafka-connect/kafka-connect.properties
      Do you want to update all detected configs? (yN) y
   
   Adding installation directory to plugin path in the following files:
   /etc/kafka/connect-distributed.properties
   /etc/kafka/connect-standalone.properties
   /etc/schema-registry/connect-avro-distributed.properties
   /etc/schema-registry/connect-avro-standalone.properties
   /etc/kafka-connect/kafka-connect.properties
   
   Completed

   ```
4. Redemarrer le connecteur

   ```bash
   docker-compose restart connect
   ```

5. A l'interieur du conteneur PostgreSQL, créer une table `users`

   ```bash
   docker-compose exec postgres psql -U myuser -d lab
   ```

   ```SQL
   CREATE TABLE users (
   id SERIAL PRIMARY KEY,
   name VARCHAR(100),
   email VARCHAR(100),
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```
6. Créez le fichier `jdbc-source-config.json` pour le connecteur JDBC Source afin d'extraire les données de cette table users et de les publier dans un topic Kafka.

   ```JSON
   {
   "name":"jdbc-source-connector",
   "config":{
   "connector.class":"io.confluent.connect.jdbc.JdbcSourceConnector",
   "tasks.max":"1",
   "connection.url":"jdbc:postgresql://postgres:5432/lab",
   "connection.user":"myuser",
   "connection.password":"mypassword",
   "table.whitelist":"users",
   "mode":"incrementing",
   "incrementing.column.name":"id",
   "poll.interval.ms":"10000",
   "key.converter": "org.apache.kafka.connect.json.JsonConverter",
   "value.converter": "org.apache.kafka.connect.json.JsonConverter",
   "key.converter.schemas.enable": "false",
   "value.converter.schemas.enable": "false"
   }
   }
   ```
   
7. Deployer le connecteur Source JDBC

   ```bash
   curl -X POST -H "Content-Type: application/json" --data @jdbc-source-config.json http://connect:8083/connectors
   ```
8. Vérifiez l'état du connecteur JDBC Source

   ```bash
   curl -X GET http://connect:8083/connectors/jdbc-source-connector/status
   ```

9. Créez le fichier `mongo-sink-config.json` pour le connecteur MongoDB Sink afin d'extraire les données de ce topic users et de les stocker dans MongoDB.

   ```JSON
   {
   "name": "mongodb-sink-connector",
   "config": {
   "connector.class": "com.mongodb.kafka.connect.MongoSinkConnector",
   "tasks.max": "1",
   "topics": "users",
   "connection.uri": "mongodb://myuser:mypassword@mongodb:27017",
   "database": "lab",
   "collection": "users",
   "key.converter": "org.apache.kafka.connect.json.JsonConverter",
   "key.converter.schemas.enable": "false",
   "value.converter": "org.apache.kafka.connect.json.JsonConverter",
   "value.converter.schemas.enable": "false"
    }
   }
   ```

10. Deployer le connecteur MongoDB Sink 

   ```bash
   curl -X POST -H "Content-Type: application/json" --data @mongo-sink-config.json http://connect:8083/connectors
   ```
11. Vérifiez l'état du connecteur Mongo Sink

   ```bash
   curl -X GET http://connect:8083/connectors/mongodb-sink-connector/status
   ```

12. Accédez au conteneur PostgreSQL et insérez des données d'exemple dans la table `users`

   ```bash
   docker-compose exec postgres psql -U myuser -d lab
   ```

   ```SQL
   INSERT INTO users (name, email) VALUES
   ('toto', 'toto@example.com'),
   ('titi', 'titi@example.com'),
   ('tata', 'tata@example.com');
   ```
13. Vérifiez les données dans MongoDB

    * A l'intérieur du conteneur
       ```bash
       docker-compose exec mongodb /bin/bash
       ```
    * Se connecter au serveur

        ```bash
        mongosh "mongodb://myuser:mypassword@mongodb"
        ```
    * utiliser la base de données `lab`
        ```bash
        use lab;
        ```
    * faire une requete `find`
        ```bash
        db.users.find();
        ```
14. Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes

   ```bash
   docker-compose down -v
   ```
   
## Lab 08 : Basic kafka stream {collapsible="true"}

* Vous allez créer une application Kafka Streams simple qui lit des données depuis un topic source
* Traiter les données
* Puis écrire les résultats dans un topic de destination

1. Exécutez la commande suivante dans le terminal pour cloner le projet (cette étape n'est pas nécessaire si vous l'avez déjà faite dans le laboratoire précédent)

   ```bash
   git clone https://github.com/MohamedKaraga/labs_kafka.git
   ```
2. Allez dans le module kafkastream
   ```bash
   cd labs_kafka/kafkastream
   ```
3. completez le code pour `StreamsConfig`, `StreamsBuilder` et `KafkaStreams`.
4. Démarrez le cluster Kafka avec l'option -d pour l'exécuter en mode détaché:
   ```bash
   docker-compose up -d broker control-center
   ```
5. Construire et exécuter kafkastream

6. Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes

   ```bash
   docker-compose down -v
   ```

## Lab 09 : KsqlDB {collapsible="true"}

1. Démarrez le cluster Kafka avec l'option -d pour l'exécuter en mode détaché:

   ```bash
   docker-compose up -d broker control-center ksqldb-server ksqldb-cli
   ```
   
2. Utilisez la console Kafka pour produire des messages vers le topic `users`

   * Exécuter dans la console du conteneur `broker`
      ```bash
      docker-compose exec broker /bin/bash
      ```

   * Exécuter le producteur

      ```bash
      kafka-console-producer --bootstrap-server kafka:9092 --topic users
      ``` 

3. Saisissez les messages JSON suivants, chacun représentant un enregistrement `user`

   ```JSON
   {"id": 1, "name": "toto", "email": "toto.doe@example.com", "created_at": "2024-06-23T12:00:00Z"}
   ```   

   ```JSON
   {"id": 2, "name": "titi", "email": "titi@example.com", "created_at": "2024-05-23T12:05:00Z"}
   ```

   ```JSON
   {"id": 3, "name": "tata", "email": "tata@example.com", "created_at": "2024-06-23T12:05:00Z"}
   ```

   ```JSON
   {"id": 4, "name": "jo", "email": "jo.doe@example.com", "created_at": "2024-05-23T12:00:00Z"}
   ```   

   ```JSON
   {"id": 5, "name": "ohe", "email": "ohe@example.com", "created_at": "2024-05-23T12:05:00Z"}
   ```

   ```JSON
   {"id": 6, "name": "yao", "email": "yao@example.com", "created_at": "2024-06-23T12:05:00Z"}
   ```
   
4. Quittez le conteneur `broker` et accédez à l'interface web du serveur ksqlDB en naviguant vers `http://localhost:8088` dans votre navigateur. Cela devrait afficher l'interface du serveur ksqlDB.

5. Accédez à l'interface en ligne de commande (CLI) de ksqlDB pour exécuter des requêtes ksqlDB

   ```bash
   docker-compose exec ksqldb-cli bash
   ```
6. A l'intérieur du shell, connectez-vous au serveur ksqlDB en utilisant l'interface en ligne de commande (CLI) de ksqlDB

   * Connectez-vous au serveur ksqlDB
   ```bash
   ksql http://ksqldb-server:8088
   ```
   * Définir l'offset sur Earliest
   ```bash
   SET 'auto.offset.reset' = 'earliest';
   ```

7. Créez un stream dans ksqlDB en utilisant la requête suivante. Par exemple, pour créer un stream pour le topic `users`

   ```SQL
   CREATE STREAM users_stream (id INT, name VARCHAR, email VARCHAR, created_at VARCHAR) WITH (KAFKA_TOPIC='users',VALUE_FORMAT='JSON');
   ```
8. Interrogez le `users_stream` pour voir les données

   ```SQL
   SELECT * FROM users_stream EMIT CHANGES;
   ```
9. Crée une table appelée `user_counts` qui stocke le nombre `users` groupés par leur `created_at`

   ```SQL
   CREATE TABLE user_counts AS
   SELECT created_at, COUNT(*) AS count
   FROM users_stream
   GROUP BY created_at;
   ```
10. Interrogez `user_counts` pour voir les données

   ```SQL
   SELECT * FROM user_counts EMIT CHANGES;
   ```

11. Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes

   ```bash
   docker-compose down -v
   ```

   
Vous pouvez m'envoyer un e-mail à [mohamedkaraga@yahoo.fr](mailto:mohamedkaraga@yahoo.fr).
