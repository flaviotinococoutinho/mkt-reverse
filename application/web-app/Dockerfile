# syntax=docker/dockerfile:1

FROM node:22-alpine

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

EXPOSE 5173

# Vite must listen on 0.0.0.0 to be reachable from outside the container.
CMD ["npm","run","dev","--","--host","0.0.0.0","--port","5173","--strictPort"]
