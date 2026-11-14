FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install --omit=dev --no-audit --no-fund
COPY src ./src
ENV PORT=8088
EXPOSE 8088
CMD ["node", "src/server.js"]
