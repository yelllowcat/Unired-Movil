import { WebSocketServer } from "ws";
import jwt from "jsonwebtoken";
import env from "../config/env.js";

/**
 * Registry: Map<userId (number), Set<WebSocket>>
 * Supports multiple simultaneous connections per user (multi-device).
 */
const userSockets = new Map();

/**
 * Attach the WebSocket server to the existing HTTP server instance.
 * @param {import("http").Server} httpServer
 */
export function initWebSocket(httpServer) {
  const wss = new WebSocketServer({ server: httpServer });

  wss.on("connection", (ws, req) => {
    // ── 1. Authenticate ──────────────────────────────────────────────────────
    const url = new URL(req.url, "http://localhost");
    const token = url.searchParams.get("token");

    if (!token) {
      ws.close(4001, "Token required");
      return;
    }

    let userId;
    try {
      const payload = jwt.verify(token, env.JWT_SECRET);
      userId = payload.userId;
    } catch {
      ws.close(4001, "Invalid or expired token");
      return;
    }

    // ── 2. Register socket ───────────────────────────────────────────────────
    if (!userSockets.has(userId)) {
      userSockets.set(userId, new Set());
    }
    userSockets.get(userId).add(ws);
    ws.userId = userId;
    ws.isAlive = true;

    console.log(`[WS] User ${userId} connected. Total sockets: ${wss.clients.size}`);

    // ── 3. Heartbeat ─────────────────────────────────────────────────────────
    ws.on("pong", () => {
      ws.isAlive = true;
    });

    // ── 4. Clean up on disconnect ─────────────────────────────────────────────
    ws.on("close", () => {
      const sockets = userSockets.get(userId);
      if (sockets) {
        sockets.delete(ws);
        if (sockets.size === 0) userSockets.delete(userId);
      }
      console.log(`[WS] User ${userId} disconnected. Total sockets: ${wss.clients.size}`);
    });

    ws.on("error", (err) => {
      console.error(`[WS] Socket error for user ${userId}:`, err.message);
    });

    // ── 5. Acknowledge connection ─────────────────────────────────────────────
    safeSend(ws, { type: "connected", data: { userId } });
  });

  // ── Heartbeat interval: ping all clients every 30s ─────────────────────────
  const heartbeatInterval = setInterval(() => {
    wss.clients.forEach((ws) => {
      if (!ws.isAlive) {
        ws.terminate();
        return;
      }
      ws.isAlive = false;
      ws.ping();
    });
  }, 30_000);

  wss.on("close", () => clearInterval(heartbeatInterval));

  console.log("[WS] WebSocket server initialized");
}

/**
 * Send a JSON message to all active sockets of a given user.
 * @param {number} userId
 * @param {object} payload
 */
export function sendToUser(userId, payload) {
  const sockets = userSockets.get(userId);
  if (!sockets || sockets.size === 0) return;

  sockets.forEach((ws) => {
    safeSend(ws, payload);
  });
}

/**
 * Safely serialise and send JSON to a WebSocket, ignoring errors.
 * @param {import("ws").WebSocket} ws
 * @param {object} payload
 */
function safeSend(ws, payload) {
  if (ws.readyState === 1 /* OPEN */) {
    try {
      ws.send(JSON.stringify(payload));
    } catch (err) {
      console.error("[WS] Failed to send message:", err.message);
    }
  }
}
