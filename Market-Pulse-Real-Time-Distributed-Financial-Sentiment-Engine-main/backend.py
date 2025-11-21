"""
FastAPI application with WebSocket support and Kafka consumer for fact-checking social media posts.
"""

import json
import os
import asyncio
import logging
from typing import Set
from contextlib import asynccontextmanager

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from aiokafka import AIOKafkaConsumer
from aiokafka.errors import KafkaError

from rag_engine import TruthEngine

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Global variables
truth_engine: TruthEngine = None
websocket_connections: Set[WebSocket] = set()
kafka_consumer_task = None

# Kafka configuration
KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092')
KAFKA_TOPIC = 'social-stream'


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Lifespan context manager for startup and shutdown events.
    """
    global truth_engine, kafka_consumer_task
    
    # Startup
    logger.info("Initializing TruthEngine...")
    try:
        truth_engine = TruthEngine()
        logger.info("TruthEngine initialized successfully")
    except Exception as e:
        logger.error(f"Failed to initialize TruthEngine: {e}")
        raise
    
    # Start Kafka consumer task
    logger.info("Starting Kafka consumer...")
    kafka_consumer_task = asyncio.create_task(consume_kafka_messages())
    
    yield
    
    # Shutdown
    logger.info("Shutting down...")
    if kafka_consumer_task:
        kafka_consumer_task.cancel()
        try:
            await kafka_consumer_task
        except asyncio.CancelledError:
            pass
    
    # Close all WebSocket connections
    for websocket in list(websocket_connections):
        try:
            await websocket.close()
        except Exception as e:
            logger.error(f"Error closing WebSocket: {e}")
    
    logger.info("Shutdown complete")


app = FastAPI(title="Social Media Fact-Checker", lifespan=lifespan)


async def consume_kafka_messages():
    """
    Asynchronous Kafka consumer that processes messages and sends them to WebSocket clients.
    """
    consumer = None
    
    while True:
        try:
            # Create Kafka consumer
            consumer = AIOKafkaConsumer(
                KAFKA_TOPIC,
                bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
                value_deserializer=lambda m: json.loads(m.decode('utf-8')),
                auto_offset_reset='latest',
                enable_auto_commit=True,
                group_id='fact-checker-group'
            )
            
            logger.info("Connecting to Kafka...")
            await consumer.start()
            logger.info(f"Connected to Kafka. Listening to topic: {KAFKA_TOPIC}")
            
            # Consume messages
            async for message in consumer:
                try:
                    # Extract post data
                    post_data = message.value
                    
                    if not isinstance(post_data, dict):
                        logger.warning(f"Received invalid message format: {post_data}")
                        continue
                    
                    headline = post_data.get('headline', '')
                    if not headline:
                        logger.warning("Post has no headline field")
                        continue
                    
                    # Run sentiment analysis
                    logger.info(f"Analyzing sentiment: {headline[:50]}...")
                    score, label = truth_engine.analyze_sentiment(headline)
                    
                    # Map sentiment label to impact
                    if label == "BULLISH":
                        impact = "POSITIVE"
                    elif label == "BEARISH":
                        impact = "NEGATIVE"
                    else:  # NEUTRAL
                        impact = "NEUTRAL"
                    
                    # Map sentiment label to status flag
                    if label == "BULLISH":
                        status = "MARKET MOVER"  # Green (HIGH impact)
                    else:  # BEARISH or NEUTRAL
                        status = "NOISE"  # Grey/Red (LOW impact)
                    
                    # Enrich the post data
                    enriched_data = {
                        **post_data,
                        'status': status,
                        'impact': impact,  # POSITIVE, NEGATIVE, or NEUTRAL
                        'similarity_score': round(score, 4),
                        'sentiment_label': label  # BULLISH, BEARISH, or NEUTRAL
                    }
                    
                    logger.info(
                        f"Post ID: {post_data.get('id', 'unknown')} - "
                        f"Status: {status} - "
                        f"Impact: {impact} - "
                        f"Sentiment: {label} - "
                        f"Score: {score:.4f}"
                    )
                    
                    # Send to all connected WebSocket clients
                    await broadcast_to_websockets(enriched_data)
                    
                except Exception as e:
                    logger.error(f"Error processing message: {e}", exc_info=True)
                    continue
                    
        except KafkaError as e:
            logger.error(f"Kafka error: {e}. Retrying in 5 seconds...")
            if consumer:
                try:
                    await consumer.stop()
                except Exception:
                    pass
            await asyncio.sleep(5)
            
        except Exception as e:
            logger.error(f"Unexpected error in Kafka consumer: {e}", exc_info=True)
            if consumer:
                try:
                    await consumer.stop()
                except Exception:
                    pass
            await asyncio.sleep(5)


async def broadcast_to_websockets(data: dict):
    """
    Broadcast data to all connected WebSocket clients.
    
    Args:
        data: Dictionary to send as JSON
    """
    if not websocket_connections:
        return
    
    message = json.dumps(data)
    disconnected = set()
    
    for websocket in websocket_connections:
        try:
            await websocket.send_text(message)
        except Exception as e:
            logger.warning(f"Error sending to WebSocket: {e}")
            disconnected.add(websocket)
    
    # Remove disconnected clients
    websocket_connections.difference_update(disconnected)


@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    """
    WebSocket endpoint for real-time fact-checking updates.
    """
    await websocket.accept()
    websocket_connections.add(websocket)
    logger.info(f"WebSocket client connected. Total connections: {len(websocket_connections)}")
    
    try:
        # Keep connection alive and handle incoming messages (if any)
        while True:
            # Wait for messages from client (or just keep connection alive)
            try:
                data = await websocket.receive_text()
                # Echo back or handle client messages if needed
                logger.debug(f"Received from client: {data}")
            except WebSocketDisconnect:
                break
                
    except WebSocketDisconnect:
        logger.info("WebSocket client disconnected")
    except Exception as e:
        logger.error(f"WebSocket error: {e}")
    finally:
        websocket_connections.discard(websocket)
        logger.info(f"WebSocket client removed. Total connections: {len(websocket_connections)}")


@app.get("/")
async def root():
    """
    Root endpoint for health check.
    """
    return {
        "status": "running",
        "service": "Social Media Fact-Checker",
        "websocket_connections": len(websocket_connections),
        "kafka_topic": KAFKA_TOPIC
    }


@app.get("/health")
async def health():
    """
    Health check endpoint.
    """
    return {
        "status": "healthy",
        "truth_engine_loaded": truth_engine is not None,
        "websocket_connections": len(websocket_connections)
    }

