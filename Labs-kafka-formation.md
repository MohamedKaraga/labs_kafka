# Labs Kafka - Formation Developer

## Lab 01 : Installation Sans Docker - 60min

### 🎯 Objectifs
- Configurer Kafka en mode KRaft
- Créer et tester des topics
- Produire et consommer des messages

### 📋 Prérequis
- **JDK 11+ installé** (JDK 17+ recommandé pour Kafka 4.0)
- Droits administrateur

**💡 Choix de version :**
- **Kafka 3.9** : Compatible avec Java 11+ (recommandé pour la compatibilité)
- **Kafka 4.0** : Nécessite obligatoirement Java 17+

### 🛠️ Instructions

#### 1. Télécharger et installer Kafka

**Option A : Kafka 3.9 (compatible Java 11+) - RECOMMANDÉ**

```bash
wget https://downloads.apache.org/kafka/3.9.2/kafka_2.13-3.9.2.tgz
tar -xzf kafka_2.13-3.9.2.tgz
cd kafka_2.13-3.9.2
```

**Option B : Kafka 4.0 (nécessite Java 17+)**

```bash
wget https://downloads.apache.org/kafka/4.0.1/kafka_2.13-4.0.1.tgz
tar -xzf kafka_2.13-4.0.1.tgz
cd kafka_2.13-4.0.1
```

**Vérifier Java**

```bash
java -version
```

#### 2. Créer les répertoires et configurer

```bash
sudo mkdir -p /var/lib/kafka/data
sudo mkdir -p /var/lib/kafka/meta
sudo chown -R $(whoami):$(whoami) /var/lib/kafka
```

**Créer la configuration**

```bash
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
bin/kafka-storage.sh format -t $(bin/kafka-storage.sh random-uuid) -c config/server.properties
```

```bash
bin/kafka-server-start.sh config/server.properties
```

#### 4. Tester dans de nouveaux terminaux

**Terminal 2 : Créer un topic**

```bash
bin/kafka-topics.sh --create --topic test --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
```

**Décrire le topic**

```bash
bin/kafka-topics.sh --describe --topic test --bootstrap-server localhost:9092
```

**Terminal 3 : Producer**

```bash
bin/kafka-console-producer.sh --topic test --bootstrap-server localhost:9092
```

**Exemples de messages à saisir :**

```
Formation Kafka avec Orsys
Hello World Kafka
{"event": "user_login", "user_id": 123, "timestamp": "2024-12-16T10:30:00Z"}
{"sensor_id": "TEMP_001", "temperature": 22.5, "location": "Paris"}
```

**Terminal 4 : Consumer**

```bash
bin/kafka-console-consumer.sh --topic test --from-beginning --bootstrap-server localhost:9092
```

### ✅ Validation
- [ ] Kafka démarre sans erreur dans le terminal 1
- [ ] Topic `test` créé avec succès
- [ ] Messages tapés dans le producer apparaissent dans le consumer

### 🔧 En cas de problème

**Vérifier Java**

```bash
java -version
```

**Installer Java 11 (Ubuntu/Debian)**

```bash
sudo apt update && sudo apt install openjdk-11-jdk
```

**Installer Java 11 (CentOS/RHEL)**

```bash
sudo yum install java-11-openjdk-devel
```

**Installer Java 11 (macOS)**

```bash
brew install openjdk@11
```

**Installer Java 17 (Ubuntu/Debian)**

```bash
sudo apt update && sudo apt install openjdk-17-jdk
```

**Installer Java 17 (CentOS/RHEL)**

```bash
sudo yum install java-17-openjdk-devel
```

**Installer Java 17 (macOS)**

```bash
brew install openjdk@17
```

**Définir JAVA_HOME**

```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
```

**macOS**

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@11
```

**Corriger les permissions**

```bash
sudo chown -R $(whoami):$(whoami) /var/lib/kafka
```

**Tuer les processus Kafka**

```bash
ps aux | grep kafka | awk '{print $2}' | xargs kill -9
```

**Nettoyer et reformater (erreur "Invalid cluster.id")**

```bash
ps aux | grep kafka | awk '{print $2}' | xargs kill -9
sudo rm -rf /var/lib/kafka/data/*
sudo rm -rf /var/lib/kafka/meta/*
bin/kafka-storage.sh format -t $(bin/kafka-storage.sh random-uuid) -c config/server.properties
bin/kafka-server-start.sh config/server.properties
```

---

## Lab 02 : Kafka avec Docker - 45min

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
wget https://raw.githubusercontent.com/MohamedKaraga/labs_kafka/refs/heads/master/docker-compose.yml
```

**Vérifier Docker**

```bash
docker ps
```

#### 2. Démarrer les services

```bash
docker-compose up -d broker control-center
```

**Vérifier le démarrage (attendre ~30 secondes)**

```bash
docker-compose ps
```

#### 3. Tester Kafka

**Entrer dans le conteneur**

```bash
docker-compose exec broker /bin/bash
```

**Créer un topic**

```bash
kafka-topics --bootstrap-server broker:9092 --create --topic test --partitions 1 --replication-factor 1
```

**Lister les topics**

```bash
kafka-topics --bootstrap-server broker:9092 --list
```

#### 4. Test Producer/Consumer

**Terminal 1 : Producer (dans le conteneur)**

```bash
kafka-console-producer --bootstrap-server broker:9092 --topic test
```

**Terminal 2 : Consumer (nouveau terminal)**

```bash
docker-compose exec broker /bin/bash
kafka-console-consumer --bootstrap-server broker:9092 --from-beginning --topic test
```

### ✅ Validation
- [ ] Conteneurs `broker` et `control-center` en statut "Up"
- [ ] Control Center accessible sur http://localhost:9021
- [ ] Messages échangés entre producer et consumer

### 🔧 En cas de problème

**Redémarrer proprement**

```bash
docker-compose down -v
docker-compose up -d broker control-center
```

**Voir les logs**

```bash
docker-compose logs broker
```

#### 5. Arrêter le cluster

```bash
docker-compose down -v
```

---

## Lab 03 : Producer - 60min

### 🎯 Objectifs
- Simuler des producteurs Kafka intégrés dans des appareils IoT
- Configurer le producteur pour publier les données des capteurs
- Configurer le batching et la compression

### 📋 Prérequis
- Java et Maven installés
- Cluster Kafka fonctionnel (Lab 02)
- Notions de programmation Java

### 🛠️ Instructions

#### 1. Cloner le projet

```bash
git clone https://github.com/MohamedKaraga/labs_kafka.git
```

#### 2. Aller dans le module producteur

```bash
cd labs_kafka/producer
```

#### 3. Compléter le code

Completez le code pour `ProducerConfig`, `ProducerRecord`, et `KafkaProducer`.

#### 4. Démarrer le cluster Kafka

```bash
docker-compose up -d broker control-center
```

#### 5. Construire et exécuter le producteur

Construire et exécuter le producteur selon vos préférences (IDE ou ligne de commande).

#### 6. Tester les configurations de performance

Exécutez le producteur avec différentes configurations pour `linger.ms` et `batch.size`.

#### 7. Configurer la compression

Configurez le type de compression (`snappy`, `gzip`, ou `lz4`).

### ✅ Validation
- [ ] Code compile et s'exécute sans erreur
- [ ] Messages des capteurs visibles dans Control Center
- [ ] Différences de performance observées
- [ ] Impact de la compression analysé

### 🔧 En cas de problème

**Vérifier les conteneurs**

```bash
docker-compose ps
```

**Voir les logs**

```bash
docker-compose logs broker
```

**Redémarrer**

```bash
docker-compose down -v
docker-compose up -d broker control-center
```

#### 8. Arrêter le cluster

```bash
docker-compose down -v
```

---

## Lab 04 : Consumer - 60min

### 🎯 Objectifs
- Configurer le consommateur Kafka
- Traiter les données entrantes
- Ajuster les paramètres de performance
- Implémenter le commit manuel

### 📋 Prérequis
- Producer du Lab 03 fonctionnel
- Java et Maven installés
- Compréhension des consumer groups

### 🛠️ Instructions

#### 1. Cloner le projet (si pas déjà fait)

```bash
git clone https://github.com/MohamedKaraga/labs_kafka.git
```

#### 2. Aller dans le module consommateur

```bash
cd labs_kafka/consumer
```

#### 3. Compléter le code

Completez le code pour `ConsumerConfig` et `KafkaConsumer`.

#### 4. Démarrer le cluster Kafka

```bash
docker-compose up -d broker control-center
```

#### 5. Construire et exécuter le consommateur

Construire et exécuter le consommateur.

#### 6. Alimenter le topic

Relancez le producer du Lab 03.

#### 7. Tester les configurations

Testez avec `fetch.min.bytes (5000000)` et `fetch.max.wait.ms (5000)`.

#### 8. Conversion vers commit manuel

Implémentez le commit manuel pour garantir le traitement "exactly-once".

### ✅ Validation
- [ ] Consumer reçoit et traite les messages
- [ ] Configurations de performance testées
- [ ] Commit manuel implémenté
- [ ] Robustesse testée
- [ ] Métriques visibles dans Control Center

### 🔧 En cas de problème

**Vérifier les consumer groups**

```bash
docker-compose exec broker kafka-consumer-groups --bootstrap-server broker:9092 --list
```

**Voir les détails du groupe**

```bash
docker-compose exec broker kafka-consumer-groups --bootstrap-server broker:9092 --describe --group [group-name]
```

**Reset des offsets**

```bash
docker-compose exec broker kafka-consumer-groups --bootstrap-server broker:9092 --group [group-name] --reset-offsets --to-earliest --topic [topic-name] --execute
```

#### 9. Arrêter le cluster

```bash
docker-compose down -v
```

---

## Lab 05 : Kafka REST Proxy - 45min

### 🎯 Objectifs
- Utiliser l'API REST de Kafka
- Comprendre l'intégration HTTP avec Kafka
- Manipuler des données JSON via REST

### 📋 Prérequis
- Docker et Docker Compose installés
- Notions de base des API REST
- Compréhension de curl

### 🛠️ Instructions

#### 1. Démarrer l'environnement avec REST Proxy

```bash
docker-compose up -d broker control-center rest-proxy
```

#### 2. Accéder au conteneur broker

```bash
docker-compose exec broker /bin/bash
```

**Créer un topic**

```bash
kafka-topics --create --topic bar --bootstrap-server broker:9092 --partitions 1 --replication-factor 1
```

**Produire un message JSON**

```bash
curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
--data '{"records":[{"value":{"name":"toto", "age":30}}]}' \
http://localhost:8082/topics/bar
```

#### 3. Consommer des messages

**Créer une instance consumer**

```bash
curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
--data '{"name": "my_consumer_instance", "format": "json", "auto.offset.reset": "earliest"}' \
http://localhost:8082/consumers/my_consumer_group
```

**Abonner le consommateur**

```bash
curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
--data '{"topics":["bar"]}' \
http://localhost:8082/consumers/my_consumer_group/instances/my_consumer_instance/subscription
```

**Consommer des messages**

```bash
curl -X GET -H "Accept: application/vnd.kafka.json.v2+json" \
http://localhost:8082/consumers/my_consumer_group/instances/my_consumer_instance/records
```

### ✅ Validation
- [ ] REST Proxy accessible sur le port 8082
- [ ] Messages JSON produits avec succès
- [ ] Messages consommés via l'API REST
- [ ] Consumer instance créée correctement

### 🔧 En cas de problème

**Vérifier REST Proxy**

```bash
docker-compose logs rest-proxy
```

**Tester la connectivité**

```bash
curl http://localhost:8082/topics
```

**Redémarrer**

```bash
docker-compose down -v
docker-compose up -d broker control-center rest-proxy
```

#### 4. Arrêter le cluster

```bash
docker-compose down -v
```

---

## Lab 06 : Schema Registry - 60min

### 🎯 Objectifs
- Comprendre la gestion des schémas
- Utiliser Apache Avro pour la sérialisation
- Transformer les producers/consumers
- Générer des classes Java depuis schémas Avro

### 📋 Prérequis
- Java et Maven installés
- Producer et Consumer des labs précédents
- Connaissance de la sérialisation

### 🛠️ Instructions

#### 1. Démarrer l'environnement avec Schema Registry

```bash
docker-compose up -d broker control-center schema-registry
```

#### 2. Enregistrer le schéma

```bash
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data '{"schema": "{\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null}]}"}' \
http://localhost:8081/subjects/user-value/versions
```

#### 3. Configuration Maven

**Ajouter le repository Maven**

```xml
<repositories>
  <repository>
    <id>confluent</id>
    <url>https://packages.confluent.io/maven/</url>
  </repository>
</repositories>
```

**Ajouter les dépendances**

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

#### 4. Définir le schéma Avro

Créer le répertoire `src/main/avro` et le fichier `user.avsc` :

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

**Ajouter le plugin Maven**

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

#### 5. Générer les classes Java

```bash
mvn clean compile
```

#### 6. Configuration Producer avec Avro

```java
Properties props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
props.put("schema.registry.url", "http://localhost:8081");

KafkaProducer<String, User> producer = new KafkaProducer<>(props);

User user = new User("John Doe", 30);

ProducerRecord<String, User> record = new ProducerRecord<>("users", user.getName().toString(), user);
```

#### 7. Configuration Consumer avec Avro

```java
Properties props = new Properties();
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-consumer-group");
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
props.put("schema.registry.url", "http://localhost:8081");
props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

KafkaConsumer<String, User> consumer = new KafkaConsumer<>(props);
```

### ✅ Validation
- [ ] Schema Registry accessible sur le port 8081
- [ ] Schéma User enregistré
- [ ] Classes Java générées
- [ ] Producer envoie des messages Avro
- [ ] Consumer désérialise correctement

### 🔧 En cas de problème

**Vérifier Schema Registry**

```bash
curl http://localhost:8081/subjects
```

**Voir les logs**

```bash
docker-compose logs schema-registry
```

**Vérifier la génération**

```bash
ls -la src/main/java/com/example/avro/
```

#### 8. Arrêter le cluster

```bash
docker-compose down -v
```

---

## Lab 07 : Deployer un kafka connect - 60min

### 🎯 Objectifs
- Déployer et configurer Kafka Connect
- Installer des connecteurs JDBC et MongoDB
- Créer un pipeline PostgreSQL → Kafka → MongoDB
- Comprendre les connecteurs Source et Sink

### 📋 Prérequis
- Docker Compose avec suffisamment de ressources
- Compréhension des concepts ETL
- Notions de PostgreSQL et MongoDB

### 🛠️ Instructions

#### 1. Démarrer l'environnement complet

```bash
docker-compose up -d broker control-center postgres connect mongodb
```

#### 2. Installer le connecteur JDBC

```bash
docker-compose exec -u root connect confluent-hub install confluentinc/kafka-connect-jdbc:10.7.6
```

**Output attendu :**
```
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

#### 3. Installer le connecteur MongoDB

```bash
docker-compose exec -u root connect confluent-hub install mongodb/kafka-connect-mongodb:latest
```

**Output attendu :**
```
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

Downloading component Kafka Connect MongoDB, provided by MongoDB, Inc. from Confluent Hub and installing into /usr/share/java/kafka
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

#### 4. Redémarrer Connect

```bash
docker-compose restart connect
```

#### 5. Préparer PostgreSQL

```bash
docker-compose exec postgres psql -U myuser -d lab
```

**Créer la table**

```sql
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100),
  email VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6. Configuration JDBC Source

Créer `jdbc-source-config.json` :

```json
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

#### 7. Déployer JDBC Source

```bash
curl -X POST -H "Content-Type: application/json" --data @jdbc-source-config.json http://connect:8083/connectors
```

#### 8. Vérifier JDBC Source

```bash
curl -X GET http://connect:8083/connectors/jdbc-source-connector/status
```

#### 9. Configuration MongoDB Sink

Créer `mongo-sink-config.json` :

```json
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

#### 10. Déployer MongoDB Sink

```bash
curl -X POST -H "Content-Type: application/json" --data @mongo-sink-config.json http://connect:8083/connectors
```

#### 11. Vérifier MongoDB Sink

```bash
curl -X GET http://connect:8083/connectors/mongodb-sink-connector/status
```

#### 12. Insérer des données de test

```bash
docker-compose exec postgres psql -U myuser -d lab
```

```sql
INSERT INTO users (name, email) VALUES
('toto', 'toto@example.com'),
('titi', 'titi@example.com'),
('tata', 'tata@example.com');
```

#### 13. Vérifier dans MongoDB

```bash
docker-compose exec mongodb /bin/bash
```

```bash
mongosh "mongodb://myuser:mypassword@mongodb"
```

```javascript
use lab;
db.users.find();
```

### ✅ Validation
- [ ] Tous les conteneurs démarrés
- [ ] Connecteurs installés et configurés
- [ ] Pipeline fonctionnel
- [ ] Données synchronisées
- [ ] Connecteurs visibles dans Control Center

### 🔧 En cas de problème

**Vérifier les connecteurs**

```bash
curl http://localhost:8083/connectors
curl http://localhost:8083/connectors/jdbc-source-connector/status
curl http://localhost:8083/connectors/mongodb-sink-connector/status
```

**Voir les logs**

```bash
docker-compose logs connect
```

**Redémarrer Connect**

```bash
docker-compose restart connect
```

**Supprimer un connecteur**

```bash
curl -X DELETE http://localhost:8083/connectors/jdbc-source-connector
```

#### 14. Arrêter le cluster

```bash
docker-compose down -v
```

---

## Lab 08 : Basic kafka stream - 60min

### 🎯 Objectifs
- Créer une application Kafka Streams
- Traiter les données en temps réel
- Écrire les résultats dans un topic
- Comprendre les transformations de flux

### 📋 Prérequis
- Java et Maven installés
- Cluster Kafka fonctionnel
- Notions de programmation réactive

### 🛠️ Instructions

#### 1. Cloner le projet (si pas déjà fait)

```bash
git clone https://github.com/MohamedKaraga/labs_kafka.git
```

#### 2. Aller dans le module kafkastream

```bash
cd labs_kafka/kafkastream
```

#### 3. Compléter le code

Completez le code pour `StreamsConfig`, `StreamsBuilder` et `KafkaStreams`.

#### 4. Démarrer le cluster Kafka

```bash
docker-compose up -d broker control-center
```

#### 5. Construire et exécuter

Construire et exécuter l'application selon vos préférences (IDE ou ligne de commande).

### ✅ Validation
- [ ] Application compile et démarre
- [ ] Topics créés
- [ ] Transformation fonctionnelle
- [ ] Flux visible dans Control Center
- [ ] Pas d'erreur dans les logs

### 🔧 En cas de problème

**Vérifier les topics**

```bash
docker-compose exec broker kafka-topics --list --bootstrap-server broker:9092
```

**Voir les consumer groups**

```bash
docker-compose exec broker kafka-consumer-groups --bootstrap-server broker:9092 --list
```

**Reset l'application**

```bash
docker-compose exec broker kafka-streams-application-reset --application-id [app-id] --bootstrap-servers broker:9092
```

#### 6. Arrêter le cluster

```bash
docker-compose down -v
```

---

## Lab 09 : KsqlDB - 60min

### 🎯 Objectifs
- Utiliser ksqlDB pour des requêtes SQL sur Kafka
- Créer des streams et tables avec SQL
- Effectuer des agrégations en temps réel
- Comprendre le streaming SQL

### 📋 Prérequis
- Connaissances SQL de base
- Cluster Kafka avec ksqlDB
- Notion de streaming vs tables

### 🛠️ Instructions

#### 1. Démarrer l'environnement avec ksqlDB

```bash
docker-compose up -d broker control-center ksqldb-server ksqldb-cli
```

#### 2. Produire des données de test

```bash
docker-compose exec broker /bin/bash
```

```bash
kafka-console-producer --bootstrap-server kafka:9092 --topic users
```

**Saisir les messages JSON :**

```json
{"id": 1, "name": "toto", "email": "toto.doe@example.com", "created_at": "2024-06-23T12:00:00Z"}
{"id": 2, "name": "titi", "email": "titi@example.com", "created_at": "2024-05-23T12:05:00Z"}
{"id": 3, "name": "tata", "email": "tata@example.com", "created_at": "2024-06-23T12:05:00Z"}
{"id": 4, "name": "jo", "email": "jo.doe@example.com", "created_at": "2024-05-23T12:00:00Z"}
{"id": 5, "name": "ohe", "email": "ohe@example.com", "created_at": "2024-05-23T12:05:00Z"}
{"id": 6, "name": "yao", "email": "yao@example.com", "created_at": "2024-06-23T12:05:00Z"}
```

#### 3. Accéder à ksqlDB CLI

```bash
docker-compose exec ksqldb-cli bash
```

```bash
ksql http://ksqldb-server:8088
```

**Configurer l'offset**

```sql
SET 'auto.offset.reset' = 'earliest';
```

#### 4. Créer un stream

```sql
CREATE STREAM users_stream (id INT, name VARCHAR, email VARCHAR, created_at VARCHAR) 
WITH (KAFKA_TOPIC='users', VALUE_FORMAT='JSON');
```

#### 5. Interroger le stream

```sql
SELECT * FROM users_stream EMIT CHANGES;
```

#### 6. Créer une table d'agrégation

```sql
CREATE TABLE user_counts AS
SELECT created_at, COUNT(*) AS count
FROM users_stream
GROUP BY created_at;
```

#### 7. Interroger la table

```sql
SELECT * FROM user_counts EMIT CHANGES;
```

### ✅ Validation
- [ ] ksqlDB Server accessible sur le port 8088
- [ ] Interface web ksqlDB fonctionnelle
- [ ] Stream `users_stream` créé avec succès
- [ ] Table `user_counts` montre les agrégations correctes
- [ ] Requêtes en temps réel fonctionnelles

### 🔧 En cas de problème

**Voir les streams et tables**

```sql
SHOW STREAMS;
SHOW TABLES;
```

**Décrire un stream**

```sql
DESCRIBE users_stream;
```

**Supprimer un stream**

```sql
DROP STREAM IF EXISTS users_stream;
```

**Voir les logs ksqlDB**

```bash
docker-compose logs ksqldb-server
```

**Redémarrer ksqlDB**

```bash
docker-compose restart ksqldb-server ksqldb-cli
```

#### 8. Arrêter le cluster

```bash
docker-compose down -v
```

---

## Contact

Pour toute question, vous pouvez me contacter à [mohamedkaraga@yahoo.fr](mailto:mohamedkaraga@yahoo.fr).
