const API_BASE_URL = "";

const stationsContainer = document.getElementById("stationsContainer");
const refreshButton = document.getElementById("refreshButton");
const connectionStatus = document.getElementById("connectionStatus");
const installButton = document.getElementById("installButton");

const totalStations = document.getElementById("totalStations");
const availableStations = document.getElementById("availableStations");
const lowStations = document.getElementById("lowStations");
const emptyStations = document.getElementById("emptyStations");
const offlineStations = document.getElementById("offlineStations");
const totalAvailableBikes = document.getElementById("totalAvailableBikes");

let deferredPrompt = null;

document.addEventListener("DOMContentLoaded", () => {
    registerServiceWorker();
    loadDashboardData();

    refreshButton.addEventListener("click", loadDashboardData);

    setInterval(loadDashboardData, 5000);
});

window.addEventListener("online", () => {
    setConnectionStatus(true);
    loadDashboardData();
});

window.addEventListener("offline", () => {
    setConnectionStatus(false);
    loadDataFromLocalStorage();
});

window.addEventListener("beforeinstallprompt", (event) => {
    event.preventDefault();
    deferredPrompt = event;
    installButton.hidden = false;
});

installButton.addEventListener("click", async () => {
    if (!deferredPrompt) {
        return;
    }

    deferredPrompt.prompt();
    await deferredPrompt.userChoice;

    deferredPrompt = null;
    installButton.hidden = true;
});

async function loadDashboardData() {
    try {
        const [stationsResponse, summaryResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/api/stations`),
            fetch(`${API_BASE_URL}/api/dashboard/summary`)
        ]);

        if (!stationsResponse.ok || !summaryResponse.ok) {
            throw new Error("No se pudo obtener información del servidor");
        }

        const stations = await stationsResponse.json();
        const summary = await summaryResponse.json();

        renderStations(stations);
        renderSummary(summary);

        localStorage.setItem("stations", JSON.stringify(stations));
        localStorage.setItem("summary", JSON.stringify(summary));

        setConnectionStatus(true);

    } catch (error) {
        console.error("[PWA] Error al consultar backend:", error);
        setConnectionStatus(false);
        loadDataFromLocalStorage();
    }
}

function loadDataFromLocalStorage() {
    const storedStations = localStorage.getItem("stations");
    const storedSummary = localStorage.getItem("summary");

    if (storedStations && storedSummary) {
        renderStations(JSON.parse(storedStations));
        renderSummary(JSON.parse(storedSummary));
    } else {
        stationsContainer.innerHTML = `
            <div class="empty-state">
                <h3>Sin datos disponibles</h3>
                <p>No hay información guardada localmente. Conéctate al servidor para cargar datos.</p>
            </div>
        `;
    }
}

function renderSummary(summary) {
    totalStations.textContent = summary.totalStations;
    availableStations.textContent = summary.availableStations;
    lowStations.textContent = summary.lowStations;
    emptyStations.textContent = summary.emptyStations;
    offlineStations.textContent = summary.offlineStations;
    totalAvailableBikes.textContent = summary.totalAvailableBikes;
}

function renderStations(stations) {
    if (!stations || stations.length === 0) {
        stationsContainer.innerHTML = `
            <div class="empty-state">
                <h3>No hay estaciones registradas</h3>
                <p>El simulador aún no ha registrado estaciones.</p>
            </div>
        `;
        return;
    }

    stationsContainer.innerHTML = stations.map(station => `
        <article class="station-card">
            <div class="station-header">
                <div>
                    <h3>${station.name}</h3>
                    <p>${station.stationId}</p>
                </div>
                <span class="badge ${getStatusClass(station.status)}">${station.status}</span>
            </div>

            <div class="bike-count">
                <span>${station.availableBikes}</span>
                <p>de ${station.capacity} bicicletas disponibles</p>
            </div>

            <div class="progress-bar">
                <div class="progress-fill" style="width: ${getPercentage(station.availableBikes, station.capacity)}%"></div>
            </div>

            <p class="last-update">
                Última actualización: ${formatDate(station.lastUpdate)}
            </p>
        </article>
    `).join("");
}

function getStatusClass(status) {
    switch (status) {
        case "AVAILABLE":
            return "badge-available";
        case "LOW":
            return "badge-low";
        case "EMPTY":
            return "badge-empty";
        case "OFFLINE":
            return "badge-offline";
        default:
            return "badge-offline";
    }
}

function getPercentage(availableBikes, capacity) {
    if (!capacity || capacity <= 0) {
        return 0;
    }

    return Math.round((availableBikes / capacity) * 100);
}

function formatDate(dateValue) {
    if (!dateValue) {
        return "Sin registro";
    }

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return dateValue;
    }

    return date.toLocaleString("es-MX", {
        dateStyle: "short",
        timeStyle: "medium"
    });
}

function setConnectionStatus(isOnline) {
    if (isOnline) {
        connectionStatus.textContent = "Online";
        connectionStatus.className = "status-online";
    } else {
        connectionStatus.textContent = "Offline";
        connectionStatus.className = "status-offline";
    }
}

function registerServiceWorker() {
    if ("serviceWorker" in navigator) {
        navigator.serviceWorker
            .register("/service-worker.js")
            .then(() => {
                console.log("[PWA] Service Worker registrado correctamente");
            })
            .catch(error => {
                console.error("[PWA] Error registrando Service Worker:", error);
            });
    }
}