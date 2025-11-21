"""
TruthEngine: A directional market sentiment analysis system using sentence transformers and FAISS.
"""

from sentence_transformers import SentenceTransformer
import faiss
import numpy as np
from typing import Tuple


class TruthEngine:
    """
    A directional market sentiment analysis engine that uses sentence embeddings and FAISS.
    """
    
    def __init__(self):
        """
        Initialize the TruthEngine with bullish and bearish axioms.
        """
        # Load the sentence transformer model
        print("Loading sentence transformer model...")
        self.model = SentenceTransformer('all-MiniLM-L6-v2')
        
        # --- IMPROVED AXIOMS (Based on your market_context.txt) ---
        
        self.bullish_axioms = [
            "Central bank interest rate cuts generally increase liquidity and boost stock prices.",
            "Low unemployment data signals a strong economy and is bullish.",
            "Strong quarterly earnings reports that beat expectations drive stock prices up.",
            "GDP growth and economic expansion are positive for markets.",
            "New product launches and tech innovation drive positive market sentiment.",
            "High consumer confidence correlates with increased spending and revenue.",
            "Positive clinical trial results boost pharmaceutical stock prices.",
            "Stable oil prices support transportation and logistics profitability.",
            "Deregulation and tax cuts often stimulate corporate growth."
        ]
        
        self.bearish_axioms = [
            "Central bank interest rate hikes decrease liquidity and lower stock prices.",
            "Rising inflation reduces consumer purchasing power and hurts retail stocks.",
            "Earnings miss expectations and lower future guidance.",
            "Recession fears and slowing economic growth are bearish.",
            "Geopolitical instability and war cause uncertainty and market sell-offs.",
            "Supply chain disruptions increase costs and hurt manufacturing revenue.",
            "Regulatory crackdowns on technology companies lower valuations.",
            "Major bankruptcies in the banking sector cause fear and contagion.",
            "Trade war tariffs increase costs for importers and are generally bearish."
        ]
        
        print(f"Loaded {len(self.bullish_axioms)} bullish axioms and {len(self.bearish_axioms)} bearish axioms.")
        
        # Convert axioms to embeddings
        print("Converting bullish axioms to embeddings...")
        bullish_embeddings = self.model.encode(self.bullish_axioms, show_progress_bar=True)
        
        print("Converting bearish axioms to embeddings...")
        bearish_embeddings = self.model.encode(self.bearish_axioms, show_progress_bar=True)
        
        # Get embedding dimension
        dimension = bullish_embeddings.shape[1]
        
        # Create FAISS indices for both
        # Bullish index
        self.bullish_index = faiss.IndexFlatIP(dimension)
        faiss.normalize_L2(bullish_embeddings)
        self.bullish_index.add(bullish_embeddings.astype('float32'))
        
        # Bearish index
        self.bearish_index = faiss.IndexFlatIP(dimension)
        faiss.normalize_L2(bearish_embeddings)
        self.bearish_index.add(bearish_embeddings.astype('float32'))
        
        print(f"FAISS indices created: {self.bullish_index.ntotal} bullish, {self.bearish_index.ntotal} bearish vectors.")
    
    def analyze_sentiment(self, text: str) -> Tuple[float, str]:
        """
        Analyze directional market sentiment by comparing text to bullish and bearish axioms.
        
        Args:
            text: The headline to analyze
            
        Returns:
            Tuple of (score, label)
            score: A value between 0 and 1 representing the confidence score
            label: "BULLISH", "BEARISH", or "NEUTRAL"
        """
        # Encode the input text
        text_embedding = self.model.encode([text])
        
        # Normalize for cosine similarity
        faiss.normalize_L2(text_embedding)
        text_embedding_float = text_embedding.astype('float32')
        
        # Calculate max similarity against bullish axioms
        bullish_similarities, _ = self.bullish_index.search(text_embedding_float, k=1)
        bull_score = float((bullish_similarities[0][0] + 1) / 2)  # Normalize to 0-1 range
        
        # Calculate max similarity against bearish axioms
        bearish_similarities, _ = self.bearish_index.search(text_embedding_float, k=1)
        bear_score = float((bearish_similarities[0][0] + 1) / 2)  # Normalize to 0-1 range
        
        # Return logic
        # Threshold set to 0.55 to filter out random noise
        if bull_score < 0.55 and bear_score < 0.55:
            label = "NEUTRAL"
            score = max(bull_score, bear_score)
        elif bull_score > bear_score:
            label = "BULLISH"
            score = bull_score
        else:
            label = "BEARISH"
            score = bear_score
        
        return score, label


if __name__ == "__main__":
    # Example usage
    engine = TruthEngine()
    
    # Test with some market headlines
    test_headlines = [
        "Central bank announces rate cuts, markets surge",
        "AAPL: New product launch expected next quarter",
        "High inflation concerns weigh on consumer spending",
        "TSLA: Stock price fluctuates on trading volume",
        "Fed raises interest rates by 0.5%",
        "GDP grows 3.2% in Q3, exceeding expectations"
    ]
    
    print("\n" + "="*60)
    print("Testing Directional Sentiment Analysis")
    print("="*60 + "\n")
    
    for headline in test_headlines:
        score, label = engine.analyze_sentiment(headline)
        print(f"Headline: {headline}")
        print(f"Score: {score:.4f}")
        print(f"Label: {label}")
        print("-" * 60)