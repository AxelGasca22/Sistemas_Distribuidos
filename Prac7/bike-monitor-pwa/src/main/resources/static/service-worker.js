const CACHE_NAME = "bike-monitor-cache-v1";

const STATIC_ASSETS = [
    "/",
    "/index.html",
    "/styles.css",
    "/app.js",
    "/manifest.json",
    "/offline.html"
];

self.addEventListener("install", event => {
    console.log("[Service Worker] Instalando...");

    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(STATIC_ASSETS))
    );

    self.skipWaiting();
});

self.addEventListener("activate", event => {
    console.log("[Service Worker] Activado");

    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames
                    .filter(cacheName => cacheName !== CACHE_NAME)
                    .map(cacheName => caches.delete(cacheName))
            );
        })
    );

    self.clients.claim();
});

self.addEventListener("fetch", event => {
    const request = event.request;

    if (request.url.includes("/api/")) {
        event.respondWith(
            fetch(request)
                .catch(() => {
                    return new Response(
                        JSON.stringify({
                            error: "Sin conexión al backend"
                        }),
                        {
                            headers: {
                                "Content-Type": "application/json"
                            }
                        }
                    );
                })
        );
        return;
    }

    event.respondWith(
        caches.match(request)
            .then(cachedResponse => {
                if (cachedResponse) {
                    return cachedResponse;
                }

                return fetch(request)
                    .catch(() => caches.match("/offline.html"));
            })
    );
});