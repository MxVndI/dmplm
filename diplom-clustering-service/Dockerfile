FROM python:3.11-slim

WORKDIR /app

# Install dependencies
COPY diplom-clustering-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application
COPY diplom-clustering-service/app.py .

# Create directory for model persistence
RUN mkdir -p /app/models

# Health check
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8085/health')"

# Run with gunicorn
CMD ["gunicorn", "-w", "2", "-b", "0.0.0.0:8085", "--timeout", "60", "-k", "uvicorn.workers.UvicornWorker", "app:app"]
