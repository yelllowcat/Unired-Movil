import { createServer } from "http";
import express from "express";
import cors from "cors";
import path from "path";
import { fileURLToPath } from "url";
import { initWebSocket } from "./websocket/wsServer.js";
import env from "./config/env.js";
import errorHandler from "./middlewares/errorHandler.js";
import ApiError from "./utils/ApiError.js";
import routes from "./routes/indexRoutes.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

app.use(cors());
app.use(express.json());
app.use("/uploads", express.static(path.join(__dirname, "../uploads")));

app.use("/api", routes);

app.use((req, res, next) => {
  next(new ApiError(404, `Not Found - ${req.originalUrl}`));
});

app.use(errorHandler);

const server = createServer(app);
initWebSocket(server);

server.listen(env.PORT, () => {
  console.log(`Server is running on port ${env.PORT}`);
});
