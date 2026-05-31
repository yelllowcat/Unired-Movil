# Build stage
FROM node:20-alpine AS builder

WORKDIR /usr/src/app

COPY package*.json ./
COPY prisma ./prisma/

RUN npm ci

# Generate Prisma Client
RUN npx prisma generate

COPY . .

# Production stage
FROM node:20-alpine

WORKDIR /usr/src/app

COPY package*.json ./
COPY --from=builder /usr/src/app/node_modules ./node_modules
COPY --from=builder /usr/src/app/prisma ./prisma
COPY --from=builder /usr/src/app/src ./src

EXPOSE 3000

ENV PORT=3000
ENV NODE_ENV=production

# Run database migrations/seeds and then start the server
CMD ["sh", "-c", "npx prisma db push && npm start"]
