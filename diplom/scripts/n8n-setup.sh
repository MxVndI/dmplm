#!/bin/bash

# Setup n8n: create credentials and import workflows

set -e

N8N_URL="${N8N_URL:-http://localhost:5678}"
TELEGRAM_BOT_TOKEN="${TELEGRAM_BOT_TOKEN}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@diplom.local}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin1234!}"

echo "⏳ Waiting for n8n to be ready..."
for i in {1..30}; do
  if curl -s "$N8N_URL/api/v1/health" > /dev/null 2>&1; then
    echo "✓ n8n is ready"
    break
  fi
  echo "  Attempt $i/30..."
  sleep 2
done

echo ""
echo "📝 Creating Telegram credential..."

# Create Telegram credential
CRED_RESPONSE=$(curl -s -X POST "$N8N_URL/api/v1/credentials" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Telegram Bot",
    "type": "telegramBot",
    "data": {
      "botToken": "'"$TELEGRAM_BOT_TOKEN"'"
    }
  }')

echo "$CRED_RESPONSE" | jq .

CRED_ID=$(echo "$CRED_RESPONSE" | jq -r '.id // empty')

if [ -z "$CRED_ID" ]; then
  echo "⚠️  Could not create credential (may already exist)"
else
  echo "✓ Telegram credential created: $CRED_ID"
fi

echo ""
echo "✅ n8n setup complete!"
echo "   URL: $N8N_URL"
echo "   Login: $ADMIN_EMAIL"
