# Lab 07 : Gestion des updates et deletes avec JDBC Source

## Contexte

Le mode `incrementing` de base ne détecte que les insertions. Pour couvrir les updates et les deletes tout en conservant JDBC, il faut modifier le schéma PostgreSQL et la configuration du connecteur.

---

## 1. Modifier la table PostgreSQL

Ajouter les colonnes `updated_at` et `deleted` :

```sql
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN deleted BOOLEAN DEFAULT FALSE;
```

---

## 2. Créer le trigger de mise à jour automatique

Ce trigger met à jour `updated_at` à chaque modification d'une ligne :

```sql
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_timestamp();
```

---

## 3. Mettre à jour la configuration JDBC Source

Remplacer le fichier `jdbc-source-config.json` par :

```json
{
  "name": "jdbc-source-connector",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "tasks.max": "1",
    "connection.url": "jdbc:postgresql://postgres:5432/lab",
    "connection.user": "myuser",
    "connection.password": "mypassword",
    "table.whitelist": "users",
    "mode": "timestamp+incrementing",
    "timestamp.column.name": "updated_at",
    "incrementing.column.name": "id",
    "poll.interval.ms": "10000",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false"
  }
}
```

Redéployer le connecteur :

```bash
curl -X DELETE http://connect:8083/connectors/jdbc-source-connector
curl -X POST -H "Content-Type: application/json" --data @jdbc-source-config.json http://connect:8083/connectors
```

---

## 4. Tester les trois opérations

Se connecter à PostgreSQL :

```bash
docker-compose exec postgres psql -U myuser -d lab
```

**Insert**

```sql
INSERT INTO users (name, email) VALUES ('toto', 'toto@example.com');
```

**Update** — le trigger met `updated_at` à jour automatiquement, JDBC détecte la ligne :

```sql
UPDATE users SET email = 'toto_new@example.com' WHERE name = 'toto';
```

**Delete** — on ne supprime pas physiquement, on marque la ligne :

```sql
UPDATE users SET deleted = TRUE WHERE name = 'toto';
```

---

## 5. Vérifier dans MongoDB

```bash
docker-compose exec mongodb /bin/bash
mongosh "mongodb://myuser:mypassword@mongodb"
use lab;
db.users.find();
```

Les documents avec `deleted: true` sont visibles côté MongoDB. La suppression physique côté MongoDB doit être gérée applicativement.

---

## Limites de cette approche

| Opération | Détection JDBC | Remarque |
|---|---|---|
| Insert | Oui | Mode `incrementing` suffit |
| Update | Oui | Nécessite la colonne `updated_at` et le trigger |
| Delete physique | Non | Invisible par nature avec JDBC |
| Soft delete | Oui | Nécessite la colonne `deleted` et une discipline applicative |

Pour les deletes physiques, la solution propre reste Debezium (CDC).
