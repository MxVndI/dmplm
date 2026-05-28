/**
 * button-test.js — Button preference A/B test metrics tracker.
 *
 * Tracks:
 * - TIME_ON_PAGE: Total time spent on the page
 * - BUTTON_CLICK: Which button was clicked (left/right) and its color
 *
 * UX:
 * - Shows thank-you message after clicking
 * - Prevents re-participation via localStorage (per testId)
 */
(function () {
    'use strict';

    const pageStartTime = Date.now();
    let buttonClicked = false;

    // ── Participation guard ───────────────────────────────────
    function getTestKey() {
        const ctx = window.__tracking || {};
        return 'btn_test_done_' + (ctx.testId || window.location.pathname);
    }

    function hasParticipated() {
        try { return !!localStorage.getItem(getTestKey()); } catch (_) { return false; }
    }

    function markParticipated(chosenColor) {
        try { localStorage.setItem(getTestKey(), chosenColor || '1'); } catch (_) {}
    }

    function showAlreadyVoted() {
        const container = document.getElementById('button-test-container');
        if (!container) return;
        container.innerHTML =
            '<div style="text-align:center;padding:1.75rem 2rem;background:#f0fdf4;border:1px solid #bbf7d0;' +
            'border-radius:1rem;max-width:380px;margin:0 auto">' +
            '<div style="font-size:2.5rem;margin-bottom:.5rem">✅</div>' +
            '<div style="font-weight:700;font-size:1.1rem;color:#166534;margin-bottom:.3rem">Вы уже проголосовали!</div>' +
            '<div style="color:#6b7280;font-size:.9rem">Спасибо за участие в тесте.</div>' +
            '</div>';
    }

    function showThankYou() {
        const container = document.getElementById('button-test-container');
        if (!container) return;
        container.innerHTML =
            '<div style="text-align:center;padding:1.75rem 2rem;background:#f0fdf4;border:1px solid #bbf7d0;' +
            'border-radius:1rem;max-width:380px;margin:0 auto">' +
            '<div style="font-size:2.5rem;margin-bottom:.5rem">🎉</div>' +
            '<div style="font-weight:700;font-size:1.1rem;color:#166534;margin-bottom:.3rem">Спасибо за ваш выбор!</div>' +
            '<div style="color:#6b7280;font-size:.9rem">Ваш голос учтён и сохранён.</div>' +
            '</div>';
    }

    // ── On load: if already participated, show locked state ──
    if (hasParticipated()) {
        showAlreadyVoted();
    } else {
        // Track button clicks
        const buttons = document.querySelectorAll('[data-button-id]');
        buttons.forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                if (buttonClicked) return; // guard against double-click

                const buttonId    = btn.dataset.buttonId;
                const buttonColor = btn.dataset.buttonColor;

                buttonClicked = true;
                markParticipated(buttonColor);
                showThankYou();

                // Send BUTTON_CLICK event to metrics
                const ctx = window.__tracking || {};
                const payload = {
                    eventType: 'BUTTON_CLICK',
                    eventData: {
                        buttonId:    buttonId,
                        buttonColor: buttonColor,
                        position:    buttonId === 'left' ? 'left' : 'right',
                        label:       btn.textContent.trim()
                    },
                    page:      window.location.pathname,
                    sessionId: getSessionId(),
                    testId:    ctx.testId  || null,
                    variant:   ctx.variant || null,
                    userAgent: navigator.userAgent
                };

                if (ctx.userId) payload.userId = ctx.userId;

                const headers = { 'Content-Type': 'application/json' };
                const csrf = getCsrf();
                if (csrf) headers[csrf.header] = csrf.token;

                fetch('/api/metrics/event', {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify(payload),
                    keepalive: true
                }).catch(function () {});
            });
        });
    }

    // ── Send TIME_ON_PAGE when leaving ────────────────────────
    window.addEventListener('pagehide', sendTimeOnPage);
    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'hidden') sendTimeOnPage();
    });

    function sendTimeOnPage() {
        const ctx = window.__tracking || {};
        const durationMs = Date.now() - pageStartTime;

        const payload = {
            eventType: 'TIME_ON_PAGE',
            eventData: { durationMs: durationMs, buttonClicked: buttonClicked },
            page:      window.location.pathname,
            sessionId: getSessionId(),
            testId:    ctx.testId  || null,
            variant:   ctx.variant || null,
            userAgent: navigator.userAgent
        };

        if (ctx.userId) payload.userId = ctx.userId;

        const headers = { 'Content-Type': 'application/json' };
        const csrf = getCsrf();
        if (csrf) headers[csrf.header] = csrf.token;

        fetch('/api/metrics/event', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(payload),
            keepalive: true
        }).catch(function () {});
    }

    // ── Session management ────────────────────────────────────
    const SESSION_KEY = 'ds_session';
    function getSessionId() {
        let sid = sessionStorage.getItem(SESSION_KEY);
        if (!sid) {
            sid = 'sess_' + Date.now() + '_' + Math.random().toString(36).slice(2, 9);
            sessionStorage.setItem(SESSION_KEY, sid);
        }
        return sid;
    }

    // ── CSRF token helper ─────────────────────────────────────
    function getCsrf() {
        const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
        if (m) return { header: 'X-XSRF-TOKEN', token: decodeURIComponent(m[1]) };

        const meta   = document.querySelector('meta[name="_csrf"]');
        const header = document.querySelector('meta[name="_csrf_header"]');
        if (meta && header) return { header: header.content, token: meta.content };

        return null;
    }
})();
