/**
 * button-test.js — Button preference A/B test metrics tracker.
 *
 * Tracks:
 * - TIME_ON_PAGE: Total time spent on the page
 * - BUTTON_CLICK: Which button was clicked (left/right) and its color
 * - Button click counts per variant
 */
(function () {
    'use strict';

    const pageStartTime = Date.now();
    let buttonClicked = false;

    // Track button clicks
    const buttons = document.querySelectorAll('[data-button-id]');
    buttons.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();

            const buttonId = btn.dataset.buttonId;
            const buttonColor = btn.dataset.buttonColor;

            buttonClicked = true;

            // Send click event
            const ctx = window.__tracking || {};
            const payload = {
                eventType: 'BUTTON_CLICK',
                eventData: {
                    buttonId: buttonId,
                    buttonColor: buttonColor,
                    position: buttonId === 'left' ? 'left' : 'right',
                    label: btn.textContent.trim()
                },
                page: window.location.pathname,
                sessionId: getSessionId(),
                testId: ctx.testId || null,
                variant: ctx.variant || null,
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

            // Add visual feedback
            btn.style.transform = 'scale(0.95)';
            setTimeout(function () {
                btn.style.transform = '';
            }, 100);
        });
    });

    // Send TIME_ON_PAGE when leaving
    window.addEventListener('pagehide', function () {
        sendTimeOnPage();
    });
    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'hidden') {
            sendTimeOnPage();
        }
    });

    function sendTimeOnPage() {
        const ctx = window.__tracking || {};
        const durationMs = Date.now() - pageStartTime;

        const payload = {
            eventType: 'TIME_ON_PAGE',
            eventData: {
                durationMs: durationMs,
                buttonClicked: buttonClicked
            },
            page: window.location.pathname,
            sessionId: getSessionId(),
            testId: ctx.testId || null,
            variant: ctx.variant || null,
            userAgent: navigator.userAgent
        };

        if (ctx.userId) payload.userId = ctx.userId;

        const headers = { 'Content-Type': 'application/json' };
        const csrf = getCsrf();
        if (csrf) headers[csrf.header] = csrf.token;

        navigator.sendBeacon
            ? fetch('/api/metrics/event', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(payload),
                keepalive: true
            }).catch(function () {})
            : fetch('/api/metrics/event', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(payload),
                keepalive: true
            }).catch(function () {});
    }

    // Session management
    const SESSION_KEY = 'ds_session';
    function getSessionId() {
        let sid = sessionStorage.getItem(SESSION_KEY);
        if (!sid) {
            sid = 'sess_' + Date.now() + '_' + Math.random().toString(36).slice(2, 9);
            sessionStorage.setItem(SESSION_KEY, sid);
        }
        return sid;
    }

    // CSRF token helper
    function getCsrf() {
        const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
        if (m) return { header: 'X-XSRF-TOKEN', token: decodeURIComponent(m[1]) };

        const meta = document.querySelector('meta[name="_csrf"]');
        const header = document.querySelector('meta[name="_csrf_header"]');
        if (meta && header) return { header: header.content, token: meta.content };

        return null;
    }
})();
