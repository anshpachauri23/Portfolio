"""
Kafka Producer for generating and sending synthetic financial news headlines.
"""

import json
import os
import time
import random
from datetime import datetime
from faker import Faker
from kafka import KafkaProducer
from kafka.errors import KafkaError, NoBrokersAvailable
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize Faker
fake = Faker()

# Kafka configuration
KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'kafka:9092').split(',')
KAFKA_TOPIC = 'social-stream'
RETRY_DELAY = 5  # seconds to wait before retrying connection
MAX_RETRIES = None  # None means infinite retries

# Market Axioms - Economic truths that represent clear signals
MARKET_AXIOMS = [
    "Central bank rate cuts generally boost stock prices.",
    "High inflation reduces consumer spending power.",
    "Rising unemployment is negative for the economy.",
    "Strong quarterly earnings reports drive stock growth.",
    "Currency devaluation makes exports more competitive.",
    "Low interest rates encourage borrowing and investment.",
    "Trade deficits can weaken a nation's currency.",
    "Oil price increases raise transportation costs globally.",
    "Government stimulus spending typically increases GDP.",
    "High corporate debt levels increase default risk."
]

# Stock tickers for noise generation
STOCK_TICKERS = [
    "AAPL", "JPM", "TSLA", "MSFT", "GOOGL", "AMZN", "META", "NVDA",
    "EUR/USD", "GBP/USD", "USD/JPY", "BTC/USD", "ETH/USD", "SPY", "QQQ"
]


def create_producer() -> KafkaProducer:
    """
    Create and return a Kafka producer with retry logic.
    
    Returns:
        KafkaProducer instance
    """
    while True:
        try:
            producer = KafkaProducer(
                bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                api_version=(0, 10, 1)
            )
            logger.info("Successfully connected to Kafka broker")
            return producer
        except NoBrokersAvailable:
            logger.warning(f"Kafka broker not available. Retrying in {RETRY_DELAY} seconds...")
            time.sleep(RETRY_DELAY)
        except KafkaError as e:
            logger.error(f"Kafka error: {e}. Retrying in {RETRY_DELAY} seconds...")
            time.sleep(RETRY_DELAY)
        except Exception as e:
            logger.error(f"Unexpected error: {e}. Retrying in {RETRY_DELAY} seconds...")
            time.sleep(RETRY_DELAY)


def generate_market_headline() -> dict:
    """
    Generate a synthetic financial news headline.
    
    Returns:
        Dictionary containing headline data with id, timestamp, ticker, and headline
    """
    # 30% chance: Pick a Market Axiom (clear signal)
    if random.random() < 0.3:
        headline = random.choice(MARKET_AXIOMS)
        ticker = "MARKET_AXIOM"
    else:
        # 70% chance: Generate noise with a random ticker
        ticker = random.choice(STOCK_TICKERS)
        headline = f"{ticker}: {fake.sentence()}"

    news_item = {
        'id': fake.uuid4(),
        'timestamp': datetime.utcnow().isoformat() + 'Z',
        'ticker': ticker,
        'headline': headline
    }
    return news_item


def main():
    """
    Main function to produce financial news headlines to Kafka.
    """
    logger.info("Starting Kafka Producer for financial news feed")
    
    # Create producer with retry logic
    producer = create_producer()
    
    headline_count = 0
    
    try:
        while True:
            # Generate a new headline
            news_item = generate_market_headline()
            headline_count += 1
            
            # Send to Kafka
            try:
                future = producer.send(KAFKA_TOPIC, value=news_item)
                # Wait for the message to be sent (optional, for error checking)
                record_metadata = future.get(timeout=10)
                
                logger.info(
                    f"Headline #{headline_count} sent - "
                    f"Ticker: {news_item['ticker']} - "
                    f"Headline Preview: {news_item['headline'][:50]}..."
                )
                
            except KafkaError as e:
                logger.error(f"Error sending message to Kafka: {e}")
                # Try to recreate producer on error
                producer.close()
                producer = create_producer()
                continue
            except Exception as e:
                logger.error(f"Unexpected error sending message: {e}")
                continue
            
            # Wait 0.5 seconds before sending next headline
            time.sleep(0.5)
            
    except KeyboardInterrupt:
        logger.info("Shutting down producer...")
    finally:
        producer.close()
        logger.info("Producer closed")


if __name__ == "__main__":
    main()
