# Battleship Deployment Guide

## The Kafka Connection Problem

The services are failing to start because Kafka isn't fully ready when WildFly tries to deploy the Kafka Resource Adapter. This is a timing issue common in microservices deployments.

## Root Cause

The error shows:
```
Caused by: org.apache.kafka.common.config.ConfigException: No resolvable bootstrap urls given in bootstrap.servers
```

This happens because:
1. Docker starts the containers
2. WildFly starts immediately and tries to deploy `kafka-rar.rar`
3. Kafka container is starting but not ready to accept connections
4. The Kafka Resource Adapter fails to initialize
5. The WAR deployments fail because they depend on the Kafka connection

## Solutions

### Solution 1: Wait for Kafka to be Ready (Recommended)

Add a health check script that waits for Kafka before starting WildFly:

1. **Create a startup script** in `battleship-service/infrastructure/wait-for-kafka.sh`:

```bash
#!/bin/bash
echo "Waiting for Kafka to be ready..."

# Wait for Kafka to be available
until nc -z kafka 29092; do
  echo "Kafka is unavailable - sleeping"
  sleep 2
done

echo "Kafka is up - starting WildFly"
exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -c standalone-full.xml
```

2. **Update Dockerfile** to use this script:

```dockerfile
# Add netcat for health checking
RUN yum install -y nc && yum clean all

COPY infrastructure/wait-for-kafka.sh /opt/jboss/
RUN chmod +x /opt/jboss/wait-for-kafka.sh

CMD ["/opt/jboss/wait-for-kafka.sh"]
```

### Solution 2: Increase Kafka Startup Wait Time

Update `docker-compose.yml` to add explicit health checks and delays:

```yaml
services:
  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    networks:
      - battleship_net
    depends_on:
      zookeeper:
        condition: service_started
    ports:
      - 9092:9092
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:29092"]
      interval: 5s
      timeout: 10s
      retries: 10
      start_period: 30s

  battleship_service:
    build:
      context: ./battleship-service
    container_name: battleship_service
    depends_on:
      kafka:
        condition: service_healthy  # Wait for Kafka to be healthy
    networks:
      - battleship_net
    ports:
      - 8080:8080
```

### Solution 3: Manual Restart (Quick Workaround)

The simplest workaround for now:

1. Let all containers start (even with errors)
2. Wait 30 seconds for Kafka to be fully ready
3. Restart just the application services:

```bash
# Wait for Kafka to be ready
sleep 30

# Restart the services (they will now connect successfully)
docker restart battleship_service battleship_computer_service

# Wait for deployment
sleep 60

# Test
curl http://localhost:8080/battleship-service/api/engineering/ping
```

### Solution 4: Use Docker Compose v3 Wait Conditions (Not Available in Compose v3.2)

Upgrade to Docker Compose file format that supports `service_healthy`:

```yaml
version: '3.9'  # Requires Docker Compose 1.29.0+

services:
  kafka:
    # ... kafka config ...
    healthcheck:
      test: ["CMD-SHELL", "kafka-broker-api-versions --bootstrap-server localhost:29092"]
      interval: 10s
      timeout: 5s
      retries: 5

  battleship_service:
    depends_on:
      kafka:
        condition: service_healthy
```

## Current Quick Fix

Since you're experiencing this issue now, use **Solution 3**:

```bash
# 1. Stop everything
docker-compose down

# 2. Start everything
docker-compose up -d

# 3. Watch Kafka logs until it's ready
docker logs -f kafka

# Look for: "INFO [KafkaServer id=1] started (kafka.server.KafkaServer)"
# Press Ctrl+C when you see this

# 4. Restart the services
docker restart battleship_service battleship_computer_service

# 5. Wait for WildFly to deploy (watch logs)
docker logs -f battleship_service

# Look for: "WFLYSRV0025: WildFly Full ... started in ... - Started XXX of XXX services"
# Press Ctrl+C when deployment completes without errors

# 6. Test the service
curl http://localhost:8080/battleship-service/api/engineering/ping
# Should return: "pong"
```

## Verification Commands

```bash
# Check all containers are running
docker ps

# Check Kafka is accessible
docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:29092

# Check service logs
docker logs battleship_service | grep -i error
docker logs battleship_computer_service | grep -i error

# Test the API
curl http://localhost:8080/battleship-service/api/engineering/ping
curl http://localhost:8081/battleship-computer-service/api/engineering/ping
```

## Playing the Game

Once the services are healthy:

```bash
cd battleship-play/target
java -jar battleship-play-1.0.1-SNAPSHOT.jar
```

## Troubleshooting

**Kafka still not connecting after restart:**
- Ensure the `battleship_net` network exists: `docker network ls | grep battleship`
- Check Kafka is listening: `docker exec kafka netstat -tuln | grep 29092`

**Services still failing:**
- Check WildFly logs: `docker exec battleship_service cat /opt/jboss/wildfly/standalone/log/server.log`
- Verify Kafka topics: `docker exec kafka kafka-topics --list --bootstrap-server localhost:29092`

**Port already in use:**
```bash
# Find what's using port 8080
lsof -i :8080
# Kill it or change docker-compose ports
```

## Long-Term Solution

For production, implement **Solution 1** with proper health checks and startup ordering. This ensures:
- Kafka is fully ready before services start
- Graceful degradation if Kafka is temporarily unavailable
- Better monitoring and observability
