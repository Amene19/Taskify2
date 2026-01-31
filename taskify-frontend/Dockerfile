# Multi-stage build for optimized image size
# Stage 1: Build
FROM node:18-alpine AS builder

# Métadonnées
LABEL maintainer="taskify-team@example.com"
LABEL version="1.0"
LABEL description="Application Taskify Frontend - Interface de gestion de tâches et rendez-vous"

WORKDIR /app

# Copy package files
COPY package.json package-lock.json ./

# Install dependencies using clean install for reproducible builds
RUN npm ci --only=production=false

# Copy source code
COPY . .

# Build argument for API URL (can be overridden at build time)
ARG REACT_APP_API_URL=http://localhost:8080/api
ENV REACT_APP_API_URL=$REACT_APP_API_URL

# Build the application
RUN npm run build

# Stage 2: Production
FROM nginx:alpine

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built application from builder stage
COPY --from=builder /app/build /usr/share/nginx/html

# Expose port 3000
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3000/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
