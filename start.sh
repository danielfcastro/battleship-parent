#!/bin/bash

echo "🚀 Starting Battleship Services..."
echo ""

# Check if network exists, if not create it
if ! docker network ls | grep -q battleship_net; then
    echo "📡 Creating Docker network: battleship_net"
    docker network create battleship_net
fi

# Stop any existing containers
echo "🛑 Stopping existing containers..."
docker-compose down 2>/dev/null

# Build and start services
echo "🔨 Building and starting services..."
docker-compose up --build -d

echo ""
echo "⏳ Services are starting. The wait-for-it.sh script in each container"
echo "   will ensure they only start WildFly after Kafka is ready."
echo ""
echo "📊 Monitoring startup progress (Ctrl+C when ready)..."
echo ""

# Follow logs to see when services are ready
docker-compose logs -f &
LOGS_PID=$!

# Wait a bit for user to see logs
sleep 90

# Kill the log following
kill $LOGS_PID 2>/dev/null

echo ""
echo "✅ Services should be ready now!"
echo ""
echo "🧪 Testing services..."
echo ""

# Test service
if curl -s http://localhost:8080/battleship-service/api/engineering/ping | grep -q "pong"; then
    echo "✅ Battleship Service: READY"
else
    echo "⚠️  Battleship Service: NOT READY YET (may need more time)"
fi

if curl -s http://localhost:8081/battleship-computer-service/api/engineering/ping | grep -q "pong"; then
    echo "✅ Computer Service: READY"
else
    echo "⚠️  Computer Service: NOT READY YET (may need more time)"
fi

echo ""
echo "🎮 To play the game:"
echo "   cd battleship-play/target"
echo "   java -jar battleship-play-1.0.1-SNAPSHOT.jar"
echo ""
echo "📋 To view logs:"
echo "   docker-compose logs -f"
echo ""
echo "🔄 If services aren't ready, wait a bit and test manually:"
echo "   curl http://localhost:8080/battleship-service/api/engineering/ping"
echo ""
