#!/usr/bin/env node

/**
 * n8n Setup Script
 * Автоматически создаёт credentials и импортирует workflows
 * Запускается после старта n8n контейнера
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const N8N_URL = process.env.N8N_URL || 'http://localhost:5678';
const TELEGRAM_BOT_TOKEN = process.env.TELEGRAM_BOT_TOKEN || '';
const GROQ_API_KEY = process.env.GROQ_API_KEY || '';
const TELEGRAM_ADMIN_CHAT_IDS = process.env.TELEGRAM_ADMIN_CHAT_IDS || '';
const TEST_SERVICE_INTERNAL_URL = process.env.TEST_SERVICE_INTERNAL_URL || 'http://test-service:8081';

const WORKFLOWS_DIR = '/workflows';
const MAX_RETRIES = 30;
const RETRY_DELAY = 2000;

async function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function httpRequest(method, path, body = null, headers = {}) {
  return new Promise((resolve, reject) => {
    const url = new URL(N8N_URL + path);
    const options = {
      hostname: url.hostname,
      port: url.port || 80,
      path: url.pathname + url.search,
      method,
      headers: { 'Content-Type': 'application/json', ...headers },
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, data: JSON.parse(data) });
        } catch {
          resolve({ status: res.statusCode, data });
        }
      });
    });

    req.on('error', reject);
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}

async function waitForN8n() {
  console.log('⏳ Waiting for n8n to be ready...');
  for (let i = 0; i < MAX_RETRIES; i++) {
    try {
      const res = await httpRequest('GET', '/api/v1/health');
      if (res.status === 200) {
        console.log('✓ n8n is ready');
        return true;
      }
    } catch (e) {
      // Retry
    }
    console.log(`  Attempt ${i + 1}/${MAX_RETRIES}...`);
    await delay(RETRY_DELAY);
  }
  throw new Error('n8n failed to start');
}

async function createTelegramCredential() {
  if (!TELEGRAM_BOT_TOKEN) {
    console.log('⚠️  TELEGRAM_BOT_TOKEN not set, skipping credential creation');
    return null;
  }

  console.log('📝 Creating Telegram credential...');
  try {
    const res = await httpRequest('POST', '/api/v1/credentials', {
      name: 'Telegram Bot Token',
      type: 'telegramBot',
      data: { botToken: TELEGRAM_BOT_TOKEN },
    });

    if (res.status === 201) {
      const credId = res.data.id;
      console.log(`✓ Telegram credential created (ID: ${credId})`);
      return credId;
    } else if (res.status === 400) {
      console.log('⚠️  Credential may already exist, continuing...');
      return null;
    } else {
      console.log(`⚠️  Unexpected response: ${res.status}`, res.data);
      return null;
    }
  } catch (err) {
    console.log(`⚠️  Could not create credential: ${err.message}`);
    return null;
  }
}

async function createGroqCredential() {
  if (!GROQ_API_KEY) {
    console.log('⚠️  GROQ_API_KEY not set, skipping credential creation');
    return null;
  }

  console.log('📝 Creating Groq credential...');
  try {
    const res = await httpRequest('POST', '/api/v1/credentials', {
      name: 'Groq API Key',
      type: 'groqApi',
      data: { apiKey: GROQ_API_KEY },
    });

    if (res.status === 201) {
      const credId = res.data.id;
      console.log(`✓ Groq credential created (ID: ${credId})`);
      return credId;
    } else {
      console.log('⚠️  Groq credential creation skipped');
      return null;
    }
  } catch (err) {
    console.log(`⚠️  Could not create Groq credential: ${err.message}`);
    return null;
  }
}

async function createConfigCredential() {
  console.log('📝 Creating config credential...');
  try {
    const res = await httpRequest('POST', '/api/v1/credentials', {
      name: 'Diplom Config',
      type: 'custom',
      data: {
        adminChatIds: TELEGRAM_ADMIN_CHAT_IDS,
        testServiceUrl: TEST_SERVICE_INTERNAL_URL,
      },
    });

    if (res.status === 201) {
      console.log(`✓ Config credential created (ID: ${res.data.id})`);
      return res.data.id;
    } else {
      console.log('⚠️  Config credential creation skipped');
      return null;
    }
  } catch (err) {
    console.log(`⚠️  Could not create config credential: ${err.message}`);
    return null;
  }
}

async function importWorkflows() {
  if (!fs.existsSync(WORKFLOWS_DIR)) {
    console.log(`ℹ️  Workflows directory not found: ${WORKFLOWS_DIR}`);
    return;
  }

  const files = fs.readdirSync(WORKFLOWS_DIR).filter(f => f.endsWith('.json'));
  if (files.length === 0) {
    console.log('ℹ️  No workflow files found');
    return;
  }

  console.log(`📦 Importing ${files.length} workflow(s)...`);
  for (const file of files) {
    try {
      const filePath = path.join(WORKFLOWS_DIR, file);
      const workflow = JSON.parse(fs.readFileSync(filePath, 'utf8'));

      console.log(`  → ${file}`);
      const res = await httpRequest('POST', '/api/v1/workflows', workflow);

      if (res.status === 201) {
        console.log(`    ✓ Imported (ID: ${res.data.id})`);
      } else {
        console.log(`    ⚠️  Status ${res.status}`);
      }
    } catch (err) {
      console.log(`    ⚠️  Error: ${err.message}`);
    }
  }
}

async function main() {
  try {
    console.log(`\n🚀 n8n Setup Script\n`);
    console.log(`N8N_URL: ${N8N_URL}`);
    console.log(`TELEGRAM_BOT_TOKEN: ${TELEGRAM_BOT_TOKEN ? '***' : 'not set'}`);
    console.log(`GROQ_API_KEY: ${GROQ_API_KEY ? '***' : 'not set'}\n`);

    await waitForN8n();
    await createTelegramCredential();
    await createGroqCredential();
    await createConfigCredential();
    await importWorkflows();

    console.log('\n✅ Setup complete!\n');
    process.exit(0);
  } catch (err) {
    console.error(`\n❌ Setup failed: ${err.message}\n`);
    process.exit(1);
  }
}

main();
