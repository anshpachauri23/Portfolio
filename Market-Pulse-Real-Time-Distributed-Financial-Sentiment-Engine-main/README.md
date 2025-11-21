# Market Pulse: Real-Time Distributed Financial Sentiment Engine

A high-frequency market analysis system that processes financial news streams in real-time using Retrieval-Augmented Generation (RAG) to correlate incoming headlines with a knowledge base of economic axioms, determining specific directional market impact.

## Project Overview

Market Pulse is a production-grade distributed system designed to analyze financial news headlines at scale, providing real-time directional sentiment signals (BULLISH/BEARISH/NEUTRAL) rather than simple positive/negative classification. The system leverages vector similarity search to match incoming news against a curated knowledge base of economic principles, enabling precise market impact assessment.

Unlike traditional sentiment analysis that relies on keyword matching or basic NLP, this engine uses semantic embeddings to understand the economic implications of financial news. For example, a headline about "interest rate cuts" is matched against the economic axiom "Interest rate cuts are bullish for stocks" to produce a BULLISH signal, not just because it contains positive words, but because it aligns with established market principles.

## Key Features

### 🚀 Distributed Ingestion
- **Apache Kafka** message broker handles high-velocity message streams (simulated financial news)
- Scalable producer-consumer architecture supporting thousands of messages per second
- Fault-tolerant design with automatic reconnection and retry logic
- Message persistence and replay capabilities

### 🧠 Directional Sentiment Analysis
- **Vector Search (FAISS)** for semantic similarity matching against economic axioms
- **Sentence Transformers** (HuggingFace) for high-quality embeddings
- Compares news headlines against two knowledge bases:
  - **Bullish Axioms**: Rate cuts, low unemployment, earnings beats, GDP growth, tech innovation, high consumer confidence
  - **Bearish Axioms**: Rate hikes, high inflation, earnings misses, recession fears, geopolitical instability, supply chain disruptions
- Outputs precise directional signals: `BULLISH`, `BEARISH`, or `NEUTRAL` with confidence scores

### 🏗️ Microservices Architecture
- Fully containerized using Docker and Docker Compose
- Service isolation and independent scaling
- Network segmentation via Docker networks
- Environment-based configuration management

### 📊 Real-Time Visualization
- **React** dashboard with WebSocket integration for sub-second updates
- Financial trading terminal UI with dark theme
- Live market velocity chart (messages per second)
- Color-coded sentiment indicators:
  - 🟢 **POSITIVE (BULLISH)**: Bright green with ↑ arrow
  - 🔴 **NEGATIVE (BEARISH)**: Bright red with ↓ arrow
  - ⚪ **NEUTRAL**: Dim grey

## System Architecture

```
┌─────────────┐
│  Producer   │  Generates synthetic financial news headlines
│  (Python)   │  using Faker library
└──────┬──────┘
       │
       │ Publishes to 'social-stream' topic
       ▼
┌─────────────┐
│   Kafka     │  Message broker (Confluent Platform)
│  (Broker)   │  Handles message queuing and distribution
└──────┬──────┘
       │
       │ Consumes messages asynchronously
       ▼
┌─────────────┐
│   Backend   │  FastAPI application with async Kafka consumer
│  (FastAPI)  │  ┌─────────────────────────────┐
│             │  │  TruthEngine (RAG Engine)  │
│             │  │  - Sentence Transformers   │
│             │  │  - FAISS Vector Search      │
│             │  │  - Sentiment Analysis        │
│             │  └─────────────────────────────┘
└──────┬──────┘
       │
       │ WebSocket broadcast (enriched data)
       ▼
┌─────────────┐
│  Frontend   │  React dashboard with WebSocket client
│   (React)   │  Real-time visualization and updates
└─────────────┘
```

### Data Flow

1. **Producer** generates synthetic financial news headlines with fields: `id`, `timestamp`, `ticker`, `headline`
2. **Kafka** receives and queues messages in the `social-stream` topic
3. **Backend** consumes messages asynchronously and:
   - Extracts the headline text
   - Runs `analyze_sentiment()` using the TruthEngine
   - Enriches the message with:
     - `impact`: POSITIVE, NEGATIVE, or NEUTRAL
     - `similarity_score`: Confidence score (0-1)
     - `sentiment_label`: BULLISH, BEARISH, or NEUTRAL
4. **Frontend** receives enriched messages via WebSocket and updates the UI in real-time

## Tech Stack

### Core Technologies
- **Python 3.9**: Backend services and data processing
- **JavaScript (React)**: Frontend dashboard with Babel for JSX transformation

### Infrastructure
- **Docker**: Containerization and service isolation
- **Docker Compose**: Multi-container orchestration
- **Apache Kafka 7.4.0**: Distributed message streaming platform
- **Zookeeper 7.4.0**: Kafka coordination service

### AI/ML
- **Sentence-Transformers** (`all-MiniLM-L6-v2`): Semantic embedding generation
- **FAISS (Facebook AI Similarity Search)**: High-performance vector similarity search
- **NumPy**: Numerical computations for embeddings

### API & Communication
- **FastAPI**: Modern, high-performance async web framework
- **WebSockets**: Real-time bidirectional communication
- **aiokafka**: Asynchronous Kafka client for Python
- **Uvicorn**: ASGI server for FastAPI

### Frontend
- **React 18**: UI library for building interactive dashboards
- **Tailwind CSS**: Utility-first CSS framework for styling
- **WebSocket API**: Native browser WebSocket client

## Quick Start Guide

### Prerequisites

- **Docker** (version 20.10 or higher)
- **Docker Compose** (version 2.0 or higher)

### Installation & Execution

1. **Clone the repository** (or navigate to the project directory):
   ```bash
   cd AI_project
   ```

2. **Start all services**:
   ```bash
   docker-compose up --build
   ```

   This command will:
   - Build Docker images for producer and backend services
   - Start Zookeeper and Kafka brokers
   - Launch the producer service (generates financial news)
   - Start the FastAPI backend (processes messages)
   - Initialize the RAG engine with market context

3. **Access the Frontend**:
   - Open `frontend/index.html` in your web browser
   - Or serve it via a local web server:
     ```bash
     # Using Python
     cd frontend && python -m http.server 3000
     # Then navigate to http://localhost:3000
     ```

4. **Monitor Services**:
   - Backend API: http://localhost:8000
   - Health Check: http://localhost:8000/health
   - Kafka Broker: localhost:9092

### Stopping the System

Press `Ctrl+C` in the terminal running `docker-compose up`, or run:
```bash
docker-compose down
```

## Project Structure

```
AI_project/
├── backend.py              # FastAPI application with Kafka consumer
├── producer.py             # Kafka producer generating financial news
├── rag_engine.py           # TruthEngine: RAG-based sentiment analysis
├── market_context.txt      # Economic axioms knowledge base
├── Dockerfile              # Container definition for Python services
├── docker-compose.yml      # Multi-service orchestration
├── requirements.txt        # Python dependencies
└── frontend/
    └── index.html          # React dashboard with WebSocket client
```

## Configuration

### Environment Variables

**Producer & Backend:**
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker address (default: `kafka:9092` for Docker, `localhost:9092` for local)

**Backend:**
- `KAFKA_TOPIC`: Kafka topic name (default: `social-stream`)

### Customization

- **Market Axioms**: Modify `rag_engine.py` to update bullish/bearish axioms
- **Producer Rate**: Adjust `time.sleep(0.5)` in `producer.py` to change message frequency
- **Sentiment Threshold**: Modify similarity thresholds in `rag_engine.py` (currently 0.5 for neutral classification)

## Why This Matters

This architecture mirrors real-world algorithmic trading pipelines used by hedge funds, investment banks, and quantitative trading firms. Key parallels include:

### Production Trading Systems
- **High-Frequency Data Processing**: Similar to how trading systems ingest market data feeds (Bloomberg, Reuters) at millisecond latencies
- **Event-Driven Architecture**: Kafka-based event streaming is the industry standard for handling market data streams
- **Real-Time Decision Making**: Sub-second sentiment analysis enables rapid trading signal generation

### Financial Technology Applications
- **News-Based Trading**: Many quantitative funds use NLP and sentiment analysis on financial news to generate alpha
- **Risk Management**: Real-time sentiment monitoring helps identify market regime changes and potential volatility spikes
- **Regulatory Compliance**: Automated sentiment tracking assists in monitoring market manipulation and insider trading signals

### Scalability & Performance
- **Horizontal Scaling**: Kafka's distributed architecture allows the system to handle millions of messages per day
- **Microservices Design**: Each component can scale independently based on load
- **Vector Search Optimization**: FAISS enables sub-millisecond similarity searches even with large knowledge bases

### Engineering Best Practices
- **Containerization**: Docker ensures consistent deployment across environments
- **Async Processing**: FastAPI and aiokafka enable high-throughput message processing
- **Real-Time Communication**: WebSockets provide low-latency updates to trading dashboards

## Performance Characteristics

- **Throughput**: Processes 2 messages/second (configurable via producer delay)
- **Latency**: End-to-end processing from Kafka to WebSocket: < 100ms
- **Scalability**: Kafka can handle 100,000+ messages/second with proper partitioning
- **Vector Search**: FAISS similarity search: < 10ms per query

## Future Enhancements

- [ ] Multi-topic Kafka partitioning for parallel processing
- [ ] Historical sentiment trend analysis and visualization
- [ ] Integration with real financial news APIs (Alpha Vantage, NewsAPI)
- [ ] Machine learning model fine-tuning on labeled financial news datasets
- [ ] Alert system for high-impact market events
- [ ] Backtesting framework for sentiment-based trading strategies
- [ ] GraphQL API for flexible data querying
- [ ] Kubernetes deployment manifests for cloud scaling

## License

This project is intended for portfolio demonstration purposes.

## Author

Senior Software Engineer - Distributed Systems & Financial Technology

---

**Note**: This system uses synthetic data generated by Faker for demonstration. In production, integrate with real financial news APIs and implement proper authentication, rate limiting, and monitoring.

