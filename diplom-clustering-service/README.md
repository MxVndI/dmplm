# Diplom Clustering Service

K-means clustering service for A/B test variant assignment. Distributes test participants based on behavioral similarity instead of random 50/50 split.

## Features

- **K-means Clustering**: Groups users into 4 behavioral clusters
- **Variant Assignment**: Clusters 0,1 → Variant A, Clusters 2,3 → Variant B
- **Feature Scaling**: StandardScaler normalization for fair clustering
- **Model Persistence**: Serializes trained models to disk
- **REST API**: FastAPI endpoints for assignment and training
- **Fallback Strategy**: Returns random 50/50 split if service unavailable

## API Endpoints

### GET /health
Health check endpoint
```bash
curl http://localhost:8085/health
```

### POST /api/cluster/assign
Assign user to cluster and variant
```bash
curl -X POST http://localhost:8085/api/cluster/assign \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "features": {
      "visitCount7Days": 5,
      "purchaseCount": 2,
      "totalSpent": 150.5,
      "cartAddCount": 8,
      "productViewCount": 20,
      "cartAbandoned": 0,
      "daysSinceLastEvent": 2.5,
      "hoursSinceLastCart": 6.0
    }
  }'
```

Response:
```json
{
  "userId": "user123",
  "clusterId": 0,
  "variant": "A",
  "distance": 0.4523
}
```

### POST /api/cluster/train
Train model on historical user data (batch operation)
```bash
curl -X POST http://localhost:8085/api/cluster/train \
  -H "Content-Type: application/json" \
  -d '[
    {
      "userId": "user1",
      "features": {...}
    },
    ...
  ]'
```

### GET /api/cluster/centroids
Get current cluster centroids
```bash
curl http://localhost:8085/api/cluster/centroids
```

## Features

8-dimensional feature vector based on UserAggregateState:
1. **visitCount7Days** (0-100): Page/product views in 7-day window
2. **purchaseCount** (0-50): Completed purchases
3. **totalSpent** (0-10000): Lifetime monetary value ($)
4. **cartAddCount** (0-100): Add-to-cart events
5. **productViewCount** (0-200): Product detail page views
6. **cartAbandoned** (0-1): Unresolved cart state (boolean)
7. **daysSinceLastEvent** (0-365): Temporal recency
8. **hoursSinceLastCart** (0-730): Cart recency in hours

## Clustering Strategy

**Default Centroids** (if no trained model available):
- Cluster 0: High activity users (10 visits, 5 purchases, $500)
- Cluster 1: Medium activity (5 visits, 2 purchases, $150, abandoned cart)
- Cluster 2: Low activity (3 visits, 1 purchase, $50, 30 days inactive)
- Cluster 3: New users (2 visits, $0, 60+ days since join)

**Training**: Use /api/cluster/train with 7+ days of historical user behavioral data

## Integration

Called from:
- **UserSelectionProcessor** (diplom-selector-service): During variant assignment in Kafka Streams topology
- **Fallback**: If clustering-service unavailable, returns random 50/50 assignment

## Running

### Docker Compose
```bash
cd diplom
docker-compose up clustering-service
```

### Local Development
```bash
pip install -r requirements.txt
python app.py
# Server runs on http://localhost:8085
```

### Production (Gunicorn)
```bash
gunicorn -w 2 -b 0.0.0.0:8085 --timeout 60 -k uvicorn.workers.UvicornWorker app:app
```

## Model Persistence

- **model.pkl**: Trained KMeans instance
- **scaler.pkl**: StandardScaler instance
- Location: `/app/` (or current directory if running locally)

Models auto-load on startup, or default centroids are used if not found.

## Configuration

| Env Var | Default | Description |
|---------|---------|-------------|
| `CLUSTERING_SERVICE_URL` | `http://localhost:8085` | Service URL (set in test-service) |

## Monitoring

Check service health:
```bash
curl http://localhost:8085/health
```

View cluster info:
```bash
curl http://localhost:8085/api/cluster/centroids | jq
```

## Future Enhancements

- [ ] Online learning: Update centroids incrementally with new user data
- [ ] Batch training: Scheduled daily retraining from historical data
- [ ] Multi-test routing: Different cluster strategies per test
- [ ] Feature importance: Explain cluster membership via SHAP values
- [ ] A/B comparison: Compare k-means vs rule-based segmentation
