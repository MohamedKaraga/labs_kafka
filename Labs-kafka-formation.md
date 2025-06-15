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
```bash
# Option A : Kafka 3.9 (compatible Java 11+) - RECOMMANDÉ
wget https://downloads.apache.org/kafka/3.9.0/kafka_2.13-3.9.0.tgz
tar -xzf kafka_2.13-3.9.0.tgz
cd kafka_2.13-3.9.0

# Option B : Kafka 4.0 (nécessite Java 17+)
# wget https://downloads.apache.org/kafka/4.0.0/kafka_2.13-4.0.0.tgz
# tar -xzf kafka_2.13-4.0.0.tgz
# cd kafka_2.13-4.0.0

# Vérifier Java (doit afficher version 11+ pour Kafka 3.9 ou 17+ pour Kafka 4.0)
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

**Exemples de messages à saisir dans le producer (Terminal 3) :**
```
Formation Kafka avec Orsys
Hello World Kafka
{"event": "user_login", "user_id": 123, "timestamp": "2024-12-16T10:30:00Z"}
{"sensor_id": "TEMP_001", "temperature": 22.5, "location": "Paris"}
```

_Les messages apparaîtront immédiatement dans le consumer (Terminal 4)_

### ✅ Validation
- [ ] Kafka démarre sans erreur dans le terminal 1
- [ ] Topic `test` créé avec succès
- [ ] Messages tapés dans le producer apparaissent dans le consumer

### 🔧 En cas de problème
```bash
# Si erreur "UnsupportedClassVersionError" ou "class file version 61.0"
# Cela signifie incompatibilité Java/Kafka
java -version

# Solutions selon la version Kafka choisie :
# Pour Kafka 3.9 : Java 11+ suffit
# Ubuntu/Debian: sudo apt update && sudo apt install openjdk-11-jdk
# CentOS/RHEL: sudo yum install java-11-openjdk-devel
# macOS: brew install openjdk@11

# Pour Kafka 4.0 : Java 17+ obligatoire
# Ubuntu/Debian: sudo apt update && sudo apt install openjdk-17-jdk
# CentOS/RHEL: sudo yum install java-17-openjdk-devel
# macOS: brew install openjdk@17

# Puis définir JAVA_HOME si nécessaire
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
# ou sur macOS: export JAVA_HOME=/opt/homebrew/opt/openjdk@11

# Si erreur de permissions
sudo chown -R $(whoami):$(whoami) /var/lib/kafka

# Si port occupé, tuer les processus Kafka
ps aux | grep kafka | awk '{print $2}' | xargs kill -9

# Si erreur "Invalid cluster.id" :
# 1. Arrêter Kafka
ps aux | grep kafka | awk '{print $2}' | xargs kill -9

# 2. Nettoyer complètement les répertoires de données
sudo rm -rf /var/lib/kafka/data/*
sudo rm -rf /var/lib/kafka/meta/*

# 3. Reformater avec un nouvel UUID
bin/kafka-storage.sh format -t $(bin/kafka-storage.sh random-uuid) -c config/server.properties

# 4. Redémarrer Kafka
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

---

## Lab 03 : Producer - 60min

### 🎯 Objectifs
- Simuler des producteurs Kafka intégrés dans des appareils IoT pour collecter les données des capteurs
- Configurer le producteur pour publier les données des capteurs dans un topic Kafka
- Configurer le batching et la compression pour optimiser la transmission des données

### 📋 Prérequis
- Java et Maven installés
- Cluster Kafka fonctionnel (Lab 02)
- Notions de programmation Java

### 🛠️ Instructions

#### 1. Cloner le projet
```bash
# Exécuter la commande suivante dans le terminal pour cloner le projet
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
# Démarrez le cluster Kafka avec l'option -d pour l'exécuter en mode détaché
docker-compose up -d broker control-center
```

#### 5. Construire et exécuter le producteur
Construire et exécuter le producteur selon vos préférences (IDE ou ligne de commande).

#### 6. Tester les configurations de performance
Exécutez le producteur avec différentes configurations pour `linger.ms` et `batch.size` afin d'observer leurs effets sur le batching des messages et le temps d'envoi.

#### 7. Configurer la compression
Configurez le type de compression pour optimiser la transmission des données (`snappy` ou `gzip` ou `lz4`).

### ✅ Validation
- [ ] Code compile et s'exécute sans erreur
- [ ] Messages des capteurs visibles dans Control Center
- [ ] Différences de performance observées avec différentes configurations
- [ ] Impact de la compression analysé

### 🔧 En cas de problème
```bash
# Vérifier l'état des conteneurs
docker-compose ps

# Voir les logs
docker-compose logs broker

# Redémarrer l'environnement
docker-compose down -v
docker-compose up -d broker control-center
```

#### 8. Arrêter le cluster
```bash
# Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes
docker-compose down -v
```

---

## Lab 04 : Consumer - 60min

### 🎯 Objectifs
- Configurer le consommateur Kafka pour ingérer les données des capteurs depuis un topic Kafka en temps réel
- Traiter les données entrantes
- Apprendre à ajuster des paramètres tels que `fetch.min.bytes`, `fetch.max.wait.ms`, `max.poll.records` pour obtenir des performances optimales
- Implémenter la conversion du commit automatique vers commit manuel

### 📋 Prérequis
- Producer du Lab 03 fonctionnel
- Java et Maven installés
- Compréhension des concepts de consumer groups

### 🛠️ Instructions

#### 1. Cloner le projet (si pas déjà fait)
```bash
# Exécuter la commande suivante dans le terminal pour cloner le projet (cette étape n'est pas nécessaire si vous l'avez déjà faite dans le lab précédent)
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
# Démarrez le cluster Kafka avec l'option -d pour l'exécuter en mode détaché
docker-compose up -d broker control-center
```

#### 5. Construire et exécuter le consommateur
Construire et exécuter le consommateur.

#### 6. Alimenter le topic
Relancez le producer utilisé dans le `Lab 03` pour alimenter le topic.

#### 7. Tester les configurations de performance
Exécutez le consommateur avec les différentes configurations pour `fetch.min.bytes (5000000)`, `fetch.max.wait.ms (5000)` pour observer leurs effets sur la consommation des messages et les performances.

#### 8. Conversion vers commit manuel
**Conversion d'un Consommateur Kafka avec Commit Automatique vers Commit Manuel**

Vous disposez actuellement d'un consommateur Kafka qui utilise le mécanisme de commit automatique des offsets. Cependant, votre équipe a identifié des problèmes potentiels de fiabilité dans le pipeline de données lors d'incidents système. Pour améliorer la robustesse de votre application, vous devez implémenter un mécanisme de commit manuel.

**Les objectifs sont :**
- Comprendre les différences entre commit automatique et manuel dans Kafka
- Implémenter une stratégie de commit manuel qui garantit le traitement "exactement une fois" (exactly-once)
- Tester la résistance de votre solution aux pannes

### ✅ Validation
- [ ] Consumer reçoit et traite les messages
- [ ] Configurations de performance testées et analysées
- [ ] Commit automatique puis manuel implémentés
- [ ] Robustesse testée (résistance aux pannes)
- [ ] Métriques visibles dans Control Center

### 🔧 En cas de problème
```bash
# Vérifier les consumer groups
docker-compose exec broker kafka-consumer-groups --bootstrap-server broker:9092 --list

# Voir les détails du groupe
docker-compose exec broker kafka-consumer-groups --bootstrap-server broker:9092 --describe --group [group-name]

# Reset des offsets si nécessaire
docker-compose exec broker kafka-consumer-groups --bootstrap-server broker:9092 --group [group-name] --reset-offsets --to-earliest --topic [topic-name] --execute
```

#### 9. Arrêter le cluster
```bash
# Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes
docker-compose down -v
```

---

## Lab 05 : Kafka REST Proxy - 45min

### 🎯 Objectifs
- Utiliser l'API REST de Kafka pour produire et consommer des messages
- Comprendre l'intégration HTTP avec Kafka
- Manipuler des données JSON via REST

### 📋 Prérequis
- Docker et Docker Compose installés
- Notions de base des API REST et JSON
- Compréhension des concepts curl

### 🛠️ Instructions

#### 1. Démarrer l'environnement avec REST Proxy
```bash
# Démarrer le cluster Kafka avec l'option -d pour l'exécuter en mode détaché
docker-compose up -d broker control-center rest-proxy
```

#### 2. Produire des messages dans un topic Kafka en utilisant le REST Proxy
```bash
# Exécuter dans la console du conteneur `broker`
docker-compose exec broker /bin/bash
```

```bash
# Créer un topic kafka
kafka-topics --create --topic bar --bootstrap-server broker:9092 --partitions 1 --replication-factor 1
```

```bash
# Produire un message JSON dans le topic
curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
--data '{"records":[{"value":{"name":"toto", "age":30}}]}' \
http://localhost:8082/topics/bar
```

#### 3. Consommer des messages dans un topic Kafka en utilisant le REST Proxy
```bash
# Créer une nouvelle instance consumer
curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
--data '{"name": "my_consumer_instance", "format": "json", "auto.offset.reset": "earliest"}' \
http://localhost:8082/consumers/my_consumer_group
```

```bash
# Abonner le consommateur au topic
curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
--data '{"topics":["bar"]}' \
http://localhost:8082/consumers/my_consumer_group/instances/my_consumer_instance/subscription
```

```bash
# Consommer des messages
curl -X GET -H "Accept: application/vnd.kafka.json.v2+json" \
http://localhost:8082/consumers/my_consumer_group/instances/my_consumer_instance/records
```

### ✅ Validation
- [ ] REST Proxy accessible sur le port 8082
- [ ] Messages JSON produits avec succès
- [ ] Messages consommés via l'API REST
- [ ] Consumer instance créée et utilisée correctement

### 🔧 En cas de problème
```bash
# Vérifier l'état du REST Proxy
docker-compose logs rest-proxy

# Tester la connectivité
curl http://localhost:8082/topics

# Redémarrer les services
docker-compose down -v
docker-compose up -d broker control-center rest-proxy
```

#### 4. Arrêter le cluster
```bash
# Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes
docker-compose down -v
```

---

## Lab 06 : Schema Registry - 60min

### 🎯 Objectifs
- Comprendre la gestion des schémas avec Schema Registry
- Utiliser Apache Avro pour la sérialisation
- Transformer les producers/consumers existants pour utiliser des schémas
- Générer des classes Java à partir de schémas Avro

### 📋 Prérequis
- Java et Maven installés
- Producer et Consumer des labs précédents
- Connaissance des concepts de sérialisation

### 🛠️ Instructions

#### 1. Démarrer l'environnement avec Schema Registry
```bash
# Démarrer le cluster Kafka avec l'option -d pour l'exécuter en mode détaché
docker-compose up -d broker control-center schema-registry
```

#### 2. Enregistrer le schéma
```bash
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data '{"schema": "{\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null}]}"}' \
http://localhost:8081/subjects/user-value/versions
```

#### 3. Produire et consommer des messages en utilisant le schéma avro

**Transformer votre producer Lab 03 et consumer Lab 04** en utilisant le schéma et l'API schema registry.

**Ajouter le repository maven de confluent :**
```xml
<repositories>
  <repository>
    <id>confluent</id>
    <url>https://packages.confluent.io/maven/</url>
  </repository>
</repositories>
```

**Ajouter les dépendances Maven Avro et Schema Registry :**
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

**Créer le répertoire `src/main/avro` et définir le schéma Avro (user.avsc) :**
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

**Ajouter le plugin Avro Maven** à votre `pom.xml` du producteur et du consommateur pour générer des classes Java à partir du schéma Avro :
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

#### 4. Générer les classes Java
```bash
# Exécuter la commande Maven pour générer les classes Java
mvn clean compile
```

#### 5. Compléter le Producer avec Avro
**Compléter et exécuter pour produire avec le schéma Avro :**
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

**Exécuter la classe Producer** et s'assurer qu'un message est produit dans le topic `users`.

#### 6. Consommer des messages en utilisant le schéma avro

**Transformer votre consommateur Lab 04** en utilisant le schéma et l'API du Schema Registry.

Ajouter les dépendances Maven Avro et Schema Registry comme précédemment.

Ajouter le plugin Avro Maven à votre `pom.xml` pour générer des classes Java à partir du schéma Avro comme précédemment.

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

**Exécuter la classe Consumer** et vérifier que le consommateur lit le message du topic `users`.

### ✅ Validation
- [ ] Schema Registry accessible sur le port 8081
- [ ] Schéma User enregistré correctement
- [ ] Classes Java générées à partir du schéma
- [ ] Producer envoie des messages Avro
- [ ] Consumer reçoit et désérialise les messages Avro

### 🔧 En cas de problème
```bash
# Vérifier Schema Registry
curl http://localhost:8081/subjects

# Voir les logs
docker-compose logs schema-registry

# Vérifier la génération des classes
ls -la src/main/java/com/example/avro/
```

#### 7. Arrêter le cluster
```bash
# Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes
docker-compose down -v
```

---

## Lab 07 : Deployer un kafka connect - 60min

### 🎯 Objectifs
- Déployer et configurer Kafka Connect
- Installer des connecteurs JDBC et MongoDB
- Créer un pipeline de données PostgreSQL → Kafka → MongoDB
- Comprendre les connecteurs Source et Sink

### 📋 Prérequis
- Docker Compose avec suffisamment de ressources
- Compréhension des concepts ETL
- Notions de PostgreSQL et MongoDB

### 🛠️ Instructions

#### 1. Démarrer l'environnement complet
```bash
# Démarrer le cluster Kafka avec l'option -d pour l'exécuter en mode détaché
docker-compose up -d broker control-center postgres connect mongodb
```

#### 2. Installer un connecteur JDBC
```bash
docker-compose exec -u root connect confluent-hub install confluentinc/kafka-connect-jdbc:10.7.6
```

**Output attendu :**
```console
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

#### 3. Installer un connecteur MongoDB
```bash
docker-compose exec -u root connect confluent-hub install mongodb/kafka-connect-mongodb:latest
```

**Output attendu :**
```console
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

#### 4. Redémarrer le connecteur
```bash
docker-compose restart connect
```

#### 5. Préparer la base PostgreSQL
```bash
# À l'intérieur du conteneur PostgreSQL, créer une table `users`
docker-compose exec postgres psql -U myuser -d lab
```

```sql
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100),
  email VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6. Configurer le connecteur JDBC Source
**Créer le fichier `jdbc-source-config.json`** pour le connecteur JDBC Source afin d'extraire les données de cette table users et de les publier dans un topic Kafka :

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

#### 7. Déployer le connecteur Source JDBC
```bash
curl -X POST -H "Content-Type: application/json" --data @jdbc-source-config.json http://connect:8083/connectors
```

#### 8. Vérifier l'état du connecteur JDBC Source
```bash
curl -X GET http://connect:8083/connectors/jdbc-source-connector/status
```

#### 9. Configurer le connecteur MongoDB Sink
**Créer le fichier `mongo-sink-config.json`** pour le connecteur MongoDB Sink afin d'extraire les données de ce topic users et de les stocker dans MongoDB :

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

#### 10. Déployer le connecteur MongoDB Sink
```bash
curl -X POST -H "Content-Type: application/json" --data @mongo-sink-config.json http://connect:8083/connectors
```

#### 11. Vérifier l'état du connecteur Mongo Sink
```bash
curl -X GET http://connect:8083/connectors/mongodb-sink-connector/status
```

#### 12. Tester le pipeline avec des données
```bash
# Accéder au conteneur PostgreSQL et insérer des données d'exemple dans la table `users`
docker-compose exec postgres psql -U myuser -d lab
```

```sql
INSERT INTO users (name, email) VALUES
('toto', 'toto@example.com'),
('titi', 'titi@example.com'),
('tata', 'tata@example.com');
```

#### 13. Vérifier les données dans MongoDB
```bash
# À l'intérieur du conteneur
docker-compose exec mongodb /bin/bash
```

```bash
# Se connecter au serveur
mongosh "mongodb://myuser:mypassword@mongodb"
```

```bash
# Utiliser la base de données `lab`
use lab;
```

```bash
# Faire une requête `find`
db.users.find();
```

### ✅ Validation
- [ ] Tous les conteneurs démarrés correctement
- [ ] Connecteurs JDBC et MongoDB installés et configurés
- [ ] Pipeline PostgreSQL → Kafka → MongoDB fonctionnel
- [ ] Données synchronisées entre PostgreSQL et MongoDB
- [ ] Connecteurs visibles dans Control Center

### 🔧 En cas de problème
```bash
# Vérifier l'état des connecteurs
curl http://localhost:8083/connectors
curl http://localhost:8083/connectors/jdbc-source-connector/status
curl http://localhost:8083/connectors/mongodb-sink-connector/status

# Voir les logs de Connect
docker-compose logs connect

# Redémarrer Connect
docker-compose restart connect

# Supprimer un connecteur si besoin
curl -X DELETE http://localhost:8083/connectors/jdbc-source-connector
```

#### 14. Arrêter le cluster
```bash
# Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes
docker-compose down -v
```

---

## Lab 08 : Basic kafka stream - 60min

### 🎯 Objectifs
- Créer une application Kafka Streams simple qui lit des données depuis un topic source
- Traiter les données en temps réel
- Écrire les résultats dans un topic de destination
- Comprendre les concepts de transformation de flux

### 📋 Prérequis
- Java et Maven installés
- Cluster Kafka fonctionnel
- Notions de programmation réactive et streaming

### 🛠️ Instructions

#### 1. Cloner le projet (si pas déjà fait)
```bash
# Exécuter la commande suivante dans le terminal pour cloner le projet (cette étape n'est pas nécessaire si vous l'avez déjà faite dans le laboratoire précédent)
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
# Démarrer le cluster Kafka avec l'option -d pour l'exécuter en mode détaché
docker-compose up -d broker control-center
```

#### 5. Construire et exécuter kafkastream
Construire et exécuter l'application Kafka Streams selon vos préférences (IDE ou ligne de commande).

### ✅ Validation
- [ ] Application Kafka Streams compile et démarre
- [ ] Topics source et destination créés
- [ ] Transformation des données fonctionnelle
- [ ] Flux de données visible dans Control Center
- [ ] Pas d'erreur dans les logs de l'application

### 🔧 En cas de problème
```bash
# Vérifier les topics
docker-compose exec broker kafka-topics --list --bootstrap-server broker:9092

# Voir les consumer groups
docker-compose exec broker kafka-consumer-groups --bootstrap-server broker:9092 --list

# Reset l'application si besoin
docker-compose exec broker kafka-streams-application-reset --application-id [app-id] --bootstrap-servers broker:9092
```

#### 6. Arrêter le cluster
```bash
# Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes
docker-compose down -v
```

---

## Lab 09 : KsqlDB - 60min

### 🎯 Objectifs
- Utiliser ksqlDB pour effectuer des requêtes SQL sur les flux Kafka
- Créer des streams et tables avec SQL
- Effectuer des agrégations en temps réel
- Comprendre les concepts de streaming SQL

### 📋 Prérequis
- Connaissances SQL de base
- Cluster Kafka avec ksqlDB
- Notion de streaming vs tables

### 🛠️ Instructions

#### 1. Démarrer l'environnement avec ksqlDB
```bash
# Démarrer le cluster Kafka avec l'option -d pour l'exécuter en mode détaché
docker-compose up -d broker control-center ksqldb-server ksqldb-cli
```

#### 2. Produire des données de test
```bash
# Utiliser la console Kafka pour produire des messages vers le topic `users`

# Exécuter dans la console du conteneur `broker`
docker-compose exec broker /bin/bash
```

```bash
# Exécuter le producteur
kafka-console-producer --bootstrap-server kafka:9092 --topic users
```

#### 3. Saisir les messages JSON
Saisir les messages JSON suivants, chacun représentant un enregistrement `user` :

```json
{"id": 1, "name": "toto", "email": "toto.doe@example.com", "created_at": "2024-06-23T12:00:00Z"}
```

```json
{"id": 2, "name": "titi", "email": "titi@example.com", "created_at": "2024-05-23T12:05:00Z"}
```

```json
{"id": 3, "name": "tata", "email": "tata@example.com", "created_at": "2024-06-23T12:05:00Z"}
```

```json
{"id": 4, "name": "jo", "email": "jo.doe@example.com", "created_at": "2024-05-23T12:00:00Z"}
```

```json
{"id": 5, "name": "ohe", "email": "ohe@example.com", "created_at": "2024-05-23T12:05:00Z"}
```

```json
{"id": 6, "name": "yao", "email": "yao@example.com", "created_at": "2024-06-23T12:05:00Z"}
```

#### 4. Accéder à l'interface ksqlDB
Quitter le conteneur `broker` et accéder à l'interface web du serveur ksqlDB en naviguant vers `http://localhost:8088` dans votre navigateur. Cela devrait afficher l'interface du serveur ksqlDB.

#### 5. Accéder à l'interface CLI de ksqlDB
```bash
# Accéder à l'interface en ligne de commande (CLI) de ksqlDB pour exécuter des requêtes ksqlDB
docker-compose exec ksqldb-cli bash
```

#### 6. Se connecter au serveur ksqlDB
```bash
# À l'intérieur du shell, connecter au serveur ksqlDB en utilisant l'interface en ligne de commande (CLI) de ksqlDB

# Connecter au serveur ksqlDB
ksql http://ksqldb-server:8088
```

```bash
# Définir l'offset sur Earliest
SET 'auto.offset.reset' = 'earliest';
```

#### 7. Créer un stream ksqlDB
```sql
-- Créer un stream dans ksqlDB en utilisant la requête suivante. Par exemple, pour créer un stream pour le topic `users`
CREATE STREAM users_stream (id INT, name VARCHAR, email VARCHAR, created_at VARCHAR) WITH (KAFKA_TOPIC='users',VALUE_FORMAT='JSON');
```

#### 8. Interroger le stream
```sql
-- Interroger le `users_stream` pour voir les données
SELECT * FROM users_stream EMIT CHANGES;
```

#### 9. Créer une table d'agrégation
```sql
-- Créer une table appelée `user_counts` qui stocke le nombre `users` groupés par leur `created_at`
CREATE TABLE user_counts AS
SELECT created_at, COUNT(*) AS count
FROM users_stream
GROUP BY created_at;
```

#### 10. Interroger la table
```sql
-- Interroger `user_counts` pour voir les données
SELECT * FROM user_counts EMIT CHANGES;
```

### ✅ Validation
- [ ] ksqlDB Server accessible sur le port 8088
- [ ] Interface web ksqlDB fonctionnelle
- [ ] Stream `users_stream` créé avec succès
- [ ] Table `user_counts` montre les agrégations correctes
- [ ] Requêtes en temps réel fonctionnelles

### 🔧 En cas de problème
```sql
-- Voir les streams et tables existants
SHOW STREAMS;
SHOW TABLES;

-- Décrire un stream
DESCRIBE users_stream;

-- Supprimer un stream si besoin
DROP STREAM IF EXISTS users_stream;
```

```bash
# Voir les logs ksqlDB
docker-compose logs ksqldb-server

# Redémarrer ksqlDB
docker-compose restart ksqldb-server ksqldb-cli
```

#### 11. Arrêter le cluster
```bash
# Arrêter le cluster Kafka avec l'option -v pour supprimer les volumes
docker-compose down -v
```

---

Vous pouvez m'envoyer un e-mail à [mohamedkaraga@yahoo.fr](mailto:mohamedkaraga@yahoo.fr).
