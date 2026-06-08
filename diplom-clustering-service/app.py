"""
K-means Clustering Service for A/B Test Variant Assignment
Distributes test participants into clusters and assigns variants based on similarity.
"""

from fastapi import FastAPI, HTTPException, Response
from pydantic import BaseModel
from typing import Dict, Optional
import logging
import numpy as np
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans
import pickle
import os
from datetime import datetime
from prometheus_client import Counter, Gauge, generate_latest, CONTENT_TYPE_LATEST

app = FastAPI(title="Diplom Clustering Service", version="1.0.0")
logger = logging.getLogger("clustering")
cluster_assignments_total = Counter(
    "clustering_assignments_total",
    "Total number of cluster assignment requests",
    ["variant"]
)
cluster_model_trained = Gauge(
    "clustering_model_trained",
    "Whether the clustering model is trained"
)
cluster_count = Gauge(
    "clustering_clusters",
    "Number of configured clusters"
)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)

class FeatureVector(BaseModel):
    """Feature vector from user aggregate state"""
    userId: str
    features: Dict[str, float]

class ClusterAssignment(BaseModel):
    """Cluster assignment result"""
    userId: str
    clusterId: int
    variant: str  # "A" or "B"
    distance: float


# Global model state
class ClusteringModel:
    def __init__(self):
        self.kmeans = None
        self.scaler = StandardScaler()
        self.feature_names = [
            'visitCount7Days',
            'purchaseCount',
            'totalSpent',
            'cartAddCount',
            'productViewCount',
            'cartAbandoned',
            'daysSinceLastEvent',
            'hoursSinceLastCart'
        ]
        self.n_clusters = 4
        self.is_trained = False
        self.load_or_initialize()

    def load_or_initialize(self):
        """Initialize with default centroids (pickle loading disabled due to sklearn version issues)"""
        logger.info("Initializing with default centroids")
        self._initialize_default()

    def _initialize_default(self):
        """Initialize with default centroids for demo"""
        # Dummy training data to initialize KMeans properly
        dummy_data = np.array([
            [10, 5, 500.0, 15, 50, 0, 2.0, 4.0],      # Cluster 0: High activity
            [5, 2, 150.0, 8, 25, 1, 7.0, 12.0],        # Cluster 1: Medium activity
            [3, 1, 50.0, 3, 10, 0, 30.0, 24.0],        # Cluster 2: Low activity
            [2, 0, 0.0, 2, 5, 0, 60.0, 48.0],          # Cluster 3: New user
            [8, 3, 300.0, 10, 35, 0, 5.0, 8.0],        # Additional for fitting
            [1, 0, 10.0, 1, 2, 1, 90.0, 72.0]          # Additional for fitting
        ])

        # Fit scaler
        self.scaler.fit(dummy_data)
        X_normalized = self.scaler.transform(dummy_data)

        # Train KMeans on dummy data (this properly initializes all internal state)
        self.kmeans = KMeans(n_clusters=self.n_clusters, random_state=42, n_init=10)
        self.kmeans.fit(X_normalized)
        self.is_trained = True
        logger.info("Initialized with default centroids")

    def normalize_features(self, features_dict: Dict[str, float]) -> np.ndarray:
        """Extract and normalize feature vector"""
        feature_vector = np.array([features_dict.get(fname, 0.0) for fname in self.feature_names])
        normalized = self.scaler.transform(feature_vector.reshape(1, -1))[0]
        return normalized

    def predict_cluster_and_variant(self, features: Dict[str, float]) -> tuple[int, str, float]:
        """Predict cluster and assign variant"""
        try:
            normalized = self.normalize_features(features)
            cluster_id = int(self.kmeans.predict([normalized])[0])
            distance = float(np.min(self.kmeans.transform([normalized])))

            # Variant assignment: Clusters 0,1 → A, Clusters 2,3 → B
            variant = "A" if cluster_id < 2 else "B"

            return cluster_id, variant, distance
        except Exception as e:
            logger.error(f"Prediction error: {e}")
            raise

    def train(self, user_features_list: list[Dict[str, float]]):
        """Train model on historical user data"""
        try:
            if len(user_features_list) < self.n_clusters:
                logger.warning(f"Not enough samples ({len(user_features_list)}) for training, keeping defaults")
                return

            # Convert to feature matrix
            X = np.array([
                [features.get(fname, 0.0) for fname in self.feature_names]
                for features in user_features_list
            ])

            # Fit scaler and transform
            self.scaler.fit(X)
            X_normalized = self.scaler.transform(X)

            # Train KMeans
            self.kmeans = KMeans(n_clusters=self.n_clusters, random_state=42, n_init=10)
            self.kmeans.fit(X_normalized)
            self.is_trained = True

            logger.info(f"Model trained on {len(user_features_list)} users, inertia: {self.kmeans.inertia_:.2f}")
        except Exception as e:
            logger.error(f"Training error: {e}")
            raise


# Initialize global model
model = ClusteringModel()


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    cluster_model_trained.set(1 if model.is_trained else 0)
    cluster_count.set(model.n_clusters)
    return {
        "status": "healthy",
        "service": "diplom-clustering",
        "model_trained": model.is_trained,
        "n_clusters": model.n_clusters
    }


@app.get("/metrics")
async def metrics():
    """Prometheus metrics endpoint"""
    cluster_model_trained.set(1 if model.is_trained else 0)
    cluster_count.set(model.n_clusters)
    return Response(generate_latest(), media_type=CONTENT_TYPE_LATEST)


@app.post("/api/cluster/assign", response_model=ClusterAssignment)
async def assign_cluster(request: FeatureVector):
    """Assign user to cluster and determine variant"""
    try:
        cluster_id, variant, distance = model.predict_cluster_and_variant(request.features)
        cluster_assignments_total.labels(variant=variant).inc()
        return ClusterAssignment(
            userId=request.userId,
            clusterId=cluster_id,
            variant=variant,
            distance=round(distance, 4)
        )
    except Exception as e:
        logger.error(f"Assignment error for user {request.userId}: {e}")
        raise HTTPException(status_code=500, detail=f"Clustering failed: {str(e)}")


@app.post("/api/cluster/train")
async def train_model(user_data: list[FeatureVector]):
    """Train model on historical user data (batch operation)"""
    try:
        features_list = [item.features for item in user_data]
        model.train(features_list)
        return {
            "status": "trained",
            "n_samples": len(features_list),
            "n_clusters": model.n_clusters,
            "timestamp": datetime.now().isoformat()
        }
    except Exception as e:
        logger.error(f"Training error: {e}")
        raise HTTPException(status_code=500, detail=f"Training failed: {str(e)}")


@app.get("/api/cluster/centroids")
async def get_centroids():
    """Get current cluster centroids"""
    if model.kmeans is None:
        raise HTTPException(status_code=400, detail="Model not trained")

    centroids = model.kmeans.cluster_centers_.tolist()
    return {
        "n_clusters": model.n_clusters,
        "feature_names": model.feature_names,
        "centroids": centroids,
        "is_trained": model.is_trained
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8085)
