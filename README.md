# Taskify - Full-Stack Task Management Application

A modern, production-ready task management application built with a complete DevOps pipeline including containerization, orchestration, and monitoring.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Taskify Application                      │
├─────────────────────────────────────────────────────────────┤
│  Frontend (React)   │   Backend (Spring Boot)   │  K8s Stack│
│  - React 18         │   - Java 17               │  - Minikube│
│  - Tailwind CSS     │   - Spring Boot 3.x       │  - Prometheus│
│  - Axios            │   - Spring Security       │  - Grafana │
│  - JWT Auth         │   - JWT Authentication    │           │
│                     │   - H2 Database           │           │
└─────────────────────────────────────────────────────────────┘
```

## Features

### Application Features
- User authentication and authorization (JWT-based)
- Task management (Create, Read, Update, Delete)
- Appointment scheduling
- Responsive UI with Tailwind CSS
- Real-time form validation

### DevOps Features
- Docker containerization
- Kubernetes orchestration with high availability
- Prometheus metrics monitoring
- Grafana dashboards for visualization
- Health checks and readiness probes
- Horizontal pod scaling
- CI/CD ready (Jenkinsfile included)

## Project Structure

```
Taskify2/
├── taskify-backend/          # Spring Boot backend application
│   ├── src/                  # Source code
│   ├── Dockerfile            # Backend container image
│   ├── Jenkinsfile           # CI/CD pipeline configuration
│   └── pom.xml               # Maven dependencies
│
├── taskify-frontend/         # React frontend application
│   ├── src/                  # React components and pages
│   ├── public/               # Static assets
│   ├── Dockerfile            # Frontend container image
│   ├── nginx.conf            # Nginx configuration
│   ├── Jenkinsfile           # CI/CD pipeline configuration
│   └── package.json          # npm dependencies
│
├── k8s/                      # Kubernetes manifests
│   ├── backend/              # Backend deployment & service
│   ├── frontend/             # Frontend deployment & service
│   ├── monitoring/           # Prometheus & Grafana configs
│   ├── namespace.yaml        # Kubernetes namespace
│   ├── deploy.sh             # Linux deployment script
│   ├── deploy.bat            # Windows deployment script
│   └── README.md             # K8s deployment guide
│
├── docker-compose.yml        # Local development with Docker Compose
├── .dockerignore             # Docker build exclusions
├── .gitignore                # Git exclusions
├── DEPLOYMENT_CHECKLIST.md   # Complete deployment verification guide
└── README.md                 # This file
```

## Quick Start

### Prerequisites
- Docker Desktop
- Minikube or Kind (for Kubernetes)
- kubectl
- Maven 3.8+ (for backend)
- Node.js 16+ (for frontend)

### Local Development with Docker Compose

```bash
# Clone the repository
git clone <your-repo-url>
cd Taskify2

# Start all services
docker-compose up --build

# Access the application
# Frontend: http://localhost:3000
# Backend: http://localhost:8080
# Backend Health: http://localhost:8080/actuator/health
```

### Kubernetes Deployment

```bash
# Start Minikube
minikube start --cpus=4 --memory=4096

# Build and load Docker images
cd taskify-backend
docker build -t taskify-backend:latest .
minikube image load taskify-backend:latest

cd ../taskify-frontend
docker build -t taskify-frontend:latest .
minikube image load taskify-frontend:latest

# Deploy to Kubernetes
cd ../k8s
./deploy.sh  # Linux/Mac
# OR
deploy.bat   # Windows

# Access services
# Frontend: http://localhost:30300
# Backend: http://localhost:30800
# Prometheus: http://localhost:30900
# Grafana: http://localhost:30301 (admin/admin)
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Tasks
- `GET /api/tasks` - Get all tasks (authenticated)
- `POST /api/tasks` - Create task (authenticated)
- `GET /api/tasks/{id}` - Get task by ID (authenticated)
- `PUT /api/tasks/{id}` - Update task (authenticated)
- `DELETE /api/tasks/{id}` - Delete task (authenticated)

### Appointments
- `GET /api/appointments` - Get all appointments (authenticated)
- `POST /api/appointments` - Create appointment (authenticated)
- `GET /api/appointments/{id}` - Get appointment by ID (authenticated)
- `PUT /api/appointments/{id}` - Update appointment (authenticated)
- `DELETE /api/appointments/{id}` - Delete appointment (authenticated)

### Actuator (Monitoring)
- `GET /actuator/health` - Health check
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /actuator/info` - Application info

## Custom Metrics

The application exposes custom business metrics:

- `taskify_users_registered_total` - Total users registered
- `taskify_users_login_attempts_total` - Total login attempts
- `taskify_users_login_success_total` - Successful logins
- `taskify_tasks_created_total` - Total tasks created
- `taskify_tasks_completed_total` - Total tasks completed
- `taskify_appointments_created_total` - Total appointments created

## Environment Variables

### Backend
```env
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
JWT_SECRET=your-secret-key-change-this-in-production
JWT_EXPIRATION=86400000
```

### Frontend
```env
REACT_APP_API_URL=http://localhost:8080/api
```

## Development

### Backend Development
```bash
cd taskify-backend

# Run tests
mvn test

# Build
mvn clean package

# Run locally
java -jar target/taskify-backend-1.0.0.jar
```

### Frontend Development
```bash
cd taskify-frontend

# Install dependencies
npm install

# Run development server
npm start

# Build for production
npm run build
```

## Monitoring

### Prometheus
Access Prometheus at `http://localhost:30900` (Kubernetes) or configured port.

Example queries:
- `taskify_tasks_created_total` - Total tasks created
- `rate(http_server_requests_seconds_count[5m])` - Request rate
- `jvm_memory_used_bytes{area="heap"}` - JVM heap memory

### Grafana
Access Grafana at `http://localhost:30301` (default: admin/admin)

Pre-configured dashboards include:
- Application metrics
- JVM metrics
- HTTP request metrics
- Custom business metrics

## Deployment Checklist

See [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) for a comprehensive step-by-step verification guide covering:
- Build verification
- Kubernetes deployment
- Health checks
- Functional testing
- Monitoring verification
- Scaling tests

## CI/CD

Jenkins pipelines are included for both frontend and backend:
- Automated builds
- Docker image creation
- Kubernetes deployment
- Health check verification

## Security Notes

- JWT tokens expire after 24 hours (configurable)
- H2 console is enabled for development only
- Change default secrets in production
- Use HTTPS in production environments
- Implement rate limiting for production

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- H2 Database (development)
- JWT for authentication
- Micrometer for metrics
- Maven

### Frontend
- React 18
- React Router v6
- Axios
- Tailwind CSS
- React Toastify

### DevOps
- Docker
- Kubernetes
- Prometheus
- Grafana
- Jenkins
- Nginx

## Troubleshooting

### Common Issues

**Pods not starting:**
```bash
kubectl get pods -n taskify
kubectl describe pod <pod-name> -n taskify
kubectl logs <pod-name> -n taskify
```

**Health checks failing:**
```bash
curl http://localhost:30800/actuator/health
```

**Minikube issues:**
```bash
minikube delete
minikube start --cpus=4 --memory=4096
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Authors

- Development Team - Initial work

## Acknowledgments

- Spring Boot documentation
- React documentation
- Kubernetes documentation
- Prometheus and Grafana communities
