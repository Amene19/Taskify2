# Taskify Kubernetes Deployment

Complete Kubernetes setup for Taskify application with Prometheus and Grafana monitoring.

## Prerequisites

- **Minikube** or **Kind** installed and running
- **kubectl** installed
- **Docker** images built:
  - `taskify-backend:latest`
  - `taskify-frontend:latest`

## Quick Start

### 1. Start Minikube

```bash
minikube start --cpus=4 --memory=4096
```

### 2. Build Docker Images

```bash
# Build backend
cd taskify-backend
mvn clean package -DskipTests
docker build -t taskify-backend:latest .

# Build frontend
cd ../taskify-frontend
docker build -t taskify-frontend:latest .
```

### 3. Load Images into Minikube

```bash
minikube image load taskify-backend:latest
minikube image load taskify-frontend:latest
```

### 4. Deploy to Kubernetes

```bash
cd ../k8s

# Linux/Mac
chmod +x deploy.sh
./deploy.sh

# Windows
deploy.bat
```

### 5. Access Applications

- **Frontend**: http://localhost:30300
- **Backend API**: http://localhost:30800
- **Backend Swagger**: http://localhost:30800/swagger-ui.html
- **Prometheus**: http://localhost:30900
- **Grafana**: http://localhost:30301
  - Username: `admin`
  - Password: `admin`

## Verify Deployment

```bash
# Check all pods are running
kubectl get pods -n taskify

# Expected output:
# NAME                               READY   STATUS    RESTARTS
# taskify-backend-xxxxx              1/1     Running   0
# taskify-backend-yyyyy              1/1     Running   0
# taskify-frontend-xxxxx             1/1     Running   0
# taskify-frontend-yyyyy             1/1     Running   0
# prometheus-xxxxx                   1/1     Running   0
# grafana-xxxxx                      1/1     Running   0

# Check services
kubectl get services -n taskify

# Check logs
kubectl logs -n taskify -l app=taskify-backend --tail=50
```

## Test the Application

### 1. Test Health Endpoints

```bash
# Frontend health
curl http://localhost:30300/health

# Backend health
curl http://localhost:30800/actuator/health

# Prometheus metrics
curl http://localhost:30800/actuator/prometheus | grep taskify
```

### 2. Test API Endpoints

```bash
# Register a user
curl -X POST http://localhost:30800/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:30800/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Save the token from login response, then create a task
TOKEN="<your-jwt-token-here>"

curl -X POST http://localhost:30800/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Test Task","description":"Testing K8s deployment","status":"TODO"}'
```

### 3. View Metrics in Prometheus

1. Open http://localhost:30900
2. Go to Graph tab
3. Try these queries:
   - `taskify_tasks_created_total` - Total tasks created
   - `taskify_users_registered_total` - Total user registrations
   - `taskify_users_login_success_total` - Successful logins
   - `rate(http_server_requests_seconds_count[5m])` - HTTP request rate

### 4. View Dashboards in Grafana

1. Open http://localhost:30301
2. Login with admin/admin
3. Go to Dashboards → Browse
4. Open "Taskify Application Metrics"
5. View panels showing HTTP requests, JVM memory, and business metrics

## Architecture

```
taskify namespace:
├── Backend (2 replicas)
│   ├── Spring Boot on port 8080
│   ├── Endpoints: /api/*, /actuator/prometheus
│   └── NodePort: 30800
├── Frontend (2 replicas)
│   ├── nginx serving React on port 3000
│   └── NodePort: 30300
├── Prometheus
│   ├── Scrapes backend metrics every 15s
│   └── NodePort: 30900
└── Grafana
    ├── Pre-configured dashboards
    └── NodePort: 30301
```

## Available Metrics

### Application Metrics (Spring Boot Actuator)
- `http_server_requests_seconds_count` - HTTP request count
- `http_server_requests_seconds_sum` - HTTP request duration
- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_gc_pause_seconds` - Garbage collection metrics
- `jvm_threads_live` - Active threads

### Custom Business Metrics
- `taskify_tasks_created_total` - Total tasks created
- `taskify_tasks_completed_total` - Total tasks marked as DONE
- `taskify_appointments_created_total` - Total appointments scheduled
- `taskify_users_registered_total` - Total user registrations
- `taskify_users_login_attempts_total` - Login attempts
- `taskify_users_login_success_total` - Successful logins

## Scaling

Scale deployments up or down:

```bash
# Scale backend to 3 replicas
kubectl scale deployment taskify-backend -n taskify --replicas=3

# Scale frontend to 3 replicas
kubectl scale deployment taskify-frontend -n taskify --replicas=3

# Verify scaling
kubectl get pods -n taskify
```

## Troubleshooting

### Pods not starting

```bash
# Check pod status
kubectl describe pod <pod-name> -n taskify

# Check logs
kubectl logs <pod-name> -n taskify

# Check events
kubectl get events -n taskify --sort-by='.lastTimestamp'
```

### Backend connection issues

```bash
# Test backend directly
kubectl port-forward -n taskify svc/taskify-backend 8080:8080

# In another terminal
curl http://localhost:8080/actuator/health
```

### Prometheus not scraping metrics

```bash
# Check Prometheus logs
kubectl logs -n taskify -l app=prometheus

# Check Prometheus targets
# Open http://localhost:30900/targets
# All targets should show "UP" status
```

### Grafana dashboard not showing data

```bash
# Check Grafana logs
kubectl logs -n taskify -l app=grafana

# Verify datasource in Grafana UI:
# Go to Configuration → Data Sources → Prometheus
# Click "Test" button - should show "Data source is working"
```

### Image pull errors (Minikube)

```bash
# Ensure images are loaded in Minikube
minikube image ls | grep taskify

# Reload if missing
minikube image load taskify-backend:latest
minikube image load taskify-frontend:latest
```

## Cleanup

Remove all Taskify resources:

```bash
kubectl delete namespace taskify
```

Or stop Minikube entirely:

```bash
minikube stop
minikube delete
```

## File Structure

```
k8s/
├── namespace.yaml              # Taskify namespace
├── backend/
│   ├── secret.yaml            # JWT secret, DB password
│   ├── configmap.yaml         # Application config
│   ├── deployment.yaml        # Backend pods (2 replicas)
│   └── service.yaml           # NodePort service (30800)
├── frontend/
│   ├── configmap.yaml         # nginx configuration
│   ├── deployment.yaml        # Frontend pods (2 replicas)
│   └── service.yaml           # NodePort service (30300)
├── monitoring/
│   ├── prometheus/
│   │   ├── clusterrole.yaml   # RBAC for metrics scraping
│   │   ├── configmap.yaml     # Prometheus config
│   │   ├── deployment.yaml    # Prometheus pod
│   │   └── service.yaml       # NodePort service (30900)
│   └── grafana/
│       ├── configmap.yaml     # Datasource + dashboards
│       ├── deployment.yaml    # Grafana pod
│       └── service.yaml       # NodePort service (30301)
├── deploy.sh                  # Deployment script (Linux/Mac)
├── deploy.bat                 # Deployment script (Windows)
└── README.md                  # This file
```

## Production Considerations

⚠️ This setup is for **local development and learning**. For production:

1. **Secrets Management**: Use Sealed Secrets, HashiCorp Vault, or cloud provider secrets
2. **Database**: Replace H2 with PostgreSQL/MySQL with PersistentVolumes
3. **Ingress**: Use Ingress controller instead of NodePort
4. **TLS/SSL**: Add certificates for HTTPS
5. **Resource Limits**: Tune based on load testing
6. **High Availability**: Increase replicas, add pod disruption budgets
7. **Monitoring**: Add alerting rules, integrate with PagerDuty/Slack
8. **Logging**: Use ELK stack or cloud logging
9. **Backups**: Implement backup strategy for Prometheus/Grafana data
10. **Image Registry**: Use private registry instead of imagePullPolicy: Never

## Next Steps

1. Explore Grafana dashboards and create custom panels
2. Set up alert rules in Prometheus
3. Test horizontal pod autoscaling
4. Implement CI/CD integration with Jenkins
5. Add more custom business metrics
6. Implement distributed tracing with Jaeger
7. Add service mesh (Istio/Linkerd) for advanced traffic management

## Support

For issues or questions:
- Check logs: `kubectl logs -n taskify <pod-name>`
- Describe resources: `kubectl describe <resource> -n taskify`
- Check events: `kubectl get events -n taskify`
