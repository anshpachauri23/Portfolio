FROM python:3.9-slim

# Set working directory
WORKDIR /app

# Copy requirements file
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application files
COPY . .

# Default command (can be overridden with docker run)
# To run backend: docker run <image> (uses default)
# To run producer: docker run <image> python producer.py
CMD ["uvicorn", "backend:app", "--host", "0.0.0.0", "--port", "8000"]

