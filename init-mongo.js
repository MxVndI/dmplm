// Initialize MongoDB databases and users

// Switch to admin database for authentication setup
db = db.getSiblingDB('admin');

// Create databases by switching to them and creating collections
db = db.getSiblingDB('diplom_shop');
db.createCollection('users');
db.createCollection('products');
db.createCollection('orders');
db.createCollection('ab_tests');
db.createCollection('user_test_participations');
db.createCollection('user_events');
db.createCollection('carts');

// Create indexes
db.users.createIndex({ login: 1 }, { unique: true });
db.users.createIndex({ email: 1 });
db.ab_tests.createIndex({ name: 1 }, { unique: true });
db.user_test_participations.createIndex({ testId: 1, userId: 1 }, { unique: true });
db.user_events.createIndex({ userId: 1, testId: 1, timestamp: 1 });

db = db.getSiblingDB('diplom_tests');
db.createCollection('ab_tests');
db.createCollection('ab_rules');
db.createCollection('test_participants');
db.createCollection('test_templates');

// Create indexes
db.ab_tests.createIndex({ name: 1 }, { unique: true });
db.ab_rules.createIndex({ testId: 1 });
db.test_participants.createIndex({ testId: 1, userId: 1 }, { unique: true });
db.test_templates.createIndex({ testId: 1 });

db = db.getSiblingDB('diplom_demographics');
db.createCollection('user_demographics');

// Create indexes
db.user_demographics.createIndex({ userId: 1 }, { unique: true });

db = db.getSiblingDB('diplom_notifications');
db.createCollection('notification_campaigns');
db.createCollection('notification_deliveries');

// Create indexes
db.notification_campaigns.createIndex({ name: 1 });
db.notification_campaigns.createIndex({ testId: 1 });
db.notification_deliveries.createIndex({ campaignId: 1, userId: 1 });
db.notification_deliveries.createIndex({ status: 1 });

db = db.getSiblingDB('diplom_selector');
db.createCollection('user_selections');

// Create indexes
db.user_selections.createIndex({ userId: 1, testId: 1 });
db.user_selections.createIndex({ createdAt: 1 });

print("All databases initialized successfully!");
