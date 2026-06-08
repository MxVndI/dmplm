(function () {
    'use strict';

    var SESSION_KEY = 'ds_session';
    function getSessionId() {
        var sid = sessionStorage.getItem(SESSION_KEY);
        if (!sid) {
            sid = 'sess_' + Date.now() + '_' + Math.random().toString(36).slice(2, 9);
            sessionStorage.setItem(SESSION_KEY, sid);
        }
        return sid;
    }

    function getCsrf() {
        var m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
        if (m) return { header: 'X-XSRF-TOKEN', token: decodeURIComponent(m[1]) };

        var meta = document.querySelector('meta[name="_csrf"]');
        var header = document.querySelector('meta[name="_csrf_header"]');
        if (meta && header) return { header: header.content, token: meta.content };

        return null;
    }

    function send(eventType, eventData) {
        var ctx = window.__tracking || {};
        var payload = {
            eventType:  eventType,
            eventData:  eventData || {},
            page:       window.location.pathname,
            sessionId:  getSessionId(),
            testId:     ctx.testId  || null,
            variant:    ctx.variant || null,
            userAgent:  navigator.userAgent
        };
        if (ctx.userId) payload.userId = ctx.userId;

        var headers = { 'Content-Type': 'application/json' };
        var csrf = getCsrf();
        if (csrf) headers[csrf.header] = csrf.token;

        navigator.sendBeacon
            ? sendBeacon(payload, headers)
            : fetch('/api/metrics/event', { method: 'POST', headers: headers,
                                             body: JSON.stringify(payload), keepalive: true });
    }

    function sendBeacon(payload, headers) {
        var csrf = getCsrf();
        var hdrs = { 'Content-Type': 'application/json' };
        if (csrf) hdrs[csrf.header] = csrf.token;
        fetch('/api/metrics/event', {
            method: 'POST', headers: hdrs,
            body: JSON.stringify(payload), keepalive: true
        }).catch(function () {});
    }

    var pageStart = Date.now();

    send('PAGE_VIEW', {
        title:    document.title,
        referrer: document.referrer || null,
        url:      window.location.href
    });

    window.addEventListener('pagehide', function () {
        send('TIME_ON_PAGE', { durationMs: Date.now() - pageStart });
    });
    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'hidden') {
            send('TIME_ON_PAGE', { durationMs: Date.now() - pageStart });
        }
    });

    var maxScroll = 0;
    var scrollTimer = null;
    window.addEventListener('scroll', function () {
        var doc = document.documentElement;
        var depth = Math.round((doc.scrollTop || document.body.scrollTop) /
            ((doc.scrollHeight || document.body.scrollHeight) - doc.clientHeight) * 100);
        if (depth > maxScroll) maxScroll = depth;

        clearTimeout(scrollTimer);
        scrollTimer = setTimeout(function () {
            send('SCROLL_DEPTH', { depth: maxScroll });
        }, 1500);
    }, { passive: true });

    document.addEventListener('click', function (e) {
        var el = e.target;
        for (var i = 0; i < 4 && el; i++, el = el.parentElement) {
            var tag = el.tagName ? el.tagName.toLowerCase() : '';
            if (['a', 'button', 'input', 'select', 'label'].includes(tag)) break;
        }
        if (!el) el = e.target;

        var data = {
            tag:    el.tagName ? el.tagName.toLowerCase() : 'unknown',
            text:   (el.innerText || el.value || el.alt || el.placeholder || '').slice(0, 80),
            href:   el.href || null,
            x:      Math.round(e.pageX),
            y:      Math.round(e.pageY),
            vx:     Math.round(e.clientX),
            vy:     Math.round(e.clientY)
        };
        var id = el.id || el.name || el.dataset.trackLabel;
        if (id) data.elementId = id;

        send('CLICK', data);
    }, true);

    var searchForms = document.querySelectorAll('form[action*="products"]');
    searchForms.forEach(function (form) {
        form.addEventListener('submit', function () {
            var q = (form.querySelector('input[name="search"]') || {}).value || '';
            send('SEARCH', { query: q.slice(0, 200) });
        });
    });

    var productMeta = document.querySelector('[data-product-id]');
    if (productMeta) {
        send('PRODUCT_VIEW', {
            productId:   productMeta.dataset.productId,
            productName: productMeta.dataset.productName || null,
            price:       productMeta.dataset.productPrice || null
        });
    }

    document.querySelectorAll('[data-track="add-to-cart"]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            send('ADD_TO_CART', {
                productId:   btn.dataset.productId,
                productName: btn.dataset.productName || null,
                price:       btn.dataset.productPrice || null,
                quantity:    btn.dataset.quantity || 1
            });
        });
    });

    document.querySelectorAll('[data-track="remove-from-cart"]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            send('CART_REMOVE', { productId: btn.dataset.productId });
        });
    });

    var checkoutForm = document.querySelector('[data-track="checkout"]');
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', function () {
            send('CHECKOUT_START', {
                totalItems: checkoutForm.dataset.cartItems || null,
                totalPrice: checkoutForm.dataset.cartTotal || null
            });
        });
    }

    document.querySelectorAll('input, textarea, select').forEach(function (field) {
        var entered = false;
        field.addEventListener('focus', function () {
            if (entered) return;
            entered = true;
            send('FORM_FOCUS', {
                fieldName: field.name || field.id || field.placeholder || 'unknown',
                fieldType: field.type || field.tagName.toLowerCase()
            });
        });
    });

})();

