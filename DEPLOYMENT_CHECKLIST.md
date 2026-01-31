# Taskify Deployment Checklist ✓

Use this checklist to ensure your complete DevOps stack is working correctly.

## Pre-Deployment Checks

- [ ] Docker Desktop is running
- [ ] Minikube or Kind is installed
- [ ] kubectl is installed
- [ ] Maven is installed (for backend build)
- [ ] Node.js is installed (for frontend build)

## Build Phase

### Backend Build
- [ ] Navigate to `taskify-backend` directory
- [ ] Run `mvn clean compile` - should succeed
- [ ] Run `mvn clean package -DskipTests` - should create JAR file
- [ ] Check `target/taskify-backend-1.0.0.jar` exists
- [ ] Run `docker build -t taskify-backend:latest .` - should succeed
- [ ] Run `docker images | grep taskify-backend` - should show image

**Verify Actuator:**
```bash
# Start backend locally (optional test)
java -jar target/taskify-backend-1.0.0.jar

# In another terminal:
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}

curl http://localhost:8080/actuator/prometheus | grep taskify
# Should show: taskify_tasks_created_total{type="task",} 0.0
```

### Frontend Build
- [ ] Navigate to `taskify-frontend` directory
- [ ] Run `npm install` - should install dependencies
- [ ] Run `npm run build` - should create `build/` directory
- [ ] Run `docker build -t taskify-frontend:latest .` - should succeed
- [ ] Run `docker images | grep taskify-frontend` - should show image

## Minikube Setup

- [ ] Run `minikube start --cpus=4 --memory=4096`
- [ ] Wait for "Done! kubectl is now configured to use minikube"
- [ ] Run `minikube status` - all should be "Running"
- [ ] Run `minikube image load taskify-backend:latest`
- [ ] Run `minikube image load taskify-frontend:latest`
- [ ] Run `minikube image ls | grep taskify` - should show both images

## Kubernetes Deployment

- [ ] Navigate to `k8s` directory
- [ ] Run deployment script (`./deploy.sh` or `deploy.bat`)
- [ ] Should see "Deployment complete!" message

**Verify Deployment:**
```bash
kubectl get namespace taskify
# Should show: taskify   Active

kubectl get pods -n taskify
# Should show 6 pods (all Running after 1-2 minutes):
# - taskify-backend-xxxxx (2 instances)
# - taskify-frontend-xxxxx (2 instances)
# - prometheus-xxxxx (1 instance)
# - grafana-xxxxx (1 instance)

kubectl get services -n taskify
# Should show 4 services with NodePorts:
# - taskify-backend   NodePort   ...   8080:30800/TCP
# - taskify-frontend  NodePort   ...   3000:30300/TCP
# - prometheus        NodePort   ...   9090:30900/TCP
# - grafana           NodePort   ...   3000:30301/TCP
```

## Health Checks

### Frontend Health
- [ ] Open browser: http://localhost:30300/health
- [ ] Should display: "healthy"
- [ ] Or run: `curl http://localhost:30300/health`

### Backend Health
- [ ] Open browser: http://localhost:30800/actuator/health
- [ ] Should show JSON: `{"status":"UP",...}`
- [ ] Or run: `curl http://localhost:30800/actuator/health`

### Backend Metrics
- [ ] Run: `curl http://localhost:30800/actuator/prometheus | grep taskify`
- [ ] Should show:
  ```
  taskify_tasks_created_total{...} 0.0
  taskify_tasks_completed_total{...} 0.0
  taskify_appointments_created_total{...} 0.0
  taskify_users_registered_total{...} 0.0
  taskify_users_login_attempts_total{...} 0.0
  taskify_users_login_success_total{...} 0.0
  ```

### Prometheus Health
- [ ] Open browser: http://localhost:30900
- [ ] Should see Prometheus UI
- [ ] Click "Status" → "Targets"
- [ ] Check `taskify-backend` job shows "UP" status

### Grafana Health
- [ ] Open browser: http://localhost:30301
- [ ] Should see Grafana login page
- [ ] Login with: admin / admin
- [ ] Should be prompted to change password (skip for now)
- [ ] Navigate to: Configuration → Data Sources → Prometheus
- [ ] Click "Test" button - should show "Data source is working"

## Functional Testing

### Test 1: User Registration
```bash
curl -X POST http://localhost:30800/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@taskify.com","password":"Test123!"}'
```
- [ ] Should return status 201 with token
- [ ] Check metric: `curl http://localhost:30800/actuator/prometheus | grep users_registered`
- [ ] Should show: `taskify_users_registered_total{...} 1.0`

### Test 2: User Login
```bash
curl -X POST http://localhost:30800/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@taskify.com","password":"Test123!"}'
```
- [ ] Should return status 200 with token
- [ ] Save the token for next tests
- [ ] Check metrics:
  ```bash
  curl http://localhost:30800/actuator/prometheus | grep login
  # Should show:
  # taskify_users_login_attempts_total{...} 1.0
  # taskify_users_login_success_total{...} 1.0
  ```

### Test 3: Create Task
```bash
# Replace TOKEN with actual token from login
TOKEN="<your-token-here>"

curl -X POST http://localhost:30800/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Test K8s Deployment",
    "description": "Verify everything works",
    "status": "TODO"
  }'
```
- [ ] Should return status 201 with task data
- [ ] Check metric: `curl http://localhost:30800/actuator/prometheus | grep tasks_created`
- [ ] Should show: `taskify_tasks_created_total{...} 1.0`

### Test 4: Complete Task
```bash
# Get task ID from previous response, then:
curl -X PUT http://localhost:30800/api/tasks/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Test K8s Deployment",
    "description": "Verify everything works",
    "status": "DONE"
  }'
```
- [ ] Should return status 200
- [ ] Check metric: `curl http://localhost:30800/actuator/prometheus | grep tasks_completed`
- [ ] Should show: `taskify_tasks_completed_total{...} 1.0`

### Test 5: Create Appointment
```bash
curl -X POST http://localhost:30800/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "subject": "Team Meeting",
    "date": "2026-02-15T10:00:00"
  }'
```
- [ ] Should return status 201
- [ ] Check metric: `curl http://localhost:30800/actuator/prometheus | grep appointments_created`
- [ ] Should show: `taskify_appointments_created_total{...} 1.0`

## Prometheus Verification

- [ ] Open: http://localhost:30900/graph
- [ ] Enter query: `taskify_tasks_created_total`
- [ ] Click "Execute"
- [ ] Should show value: 1

**Try these queries:**
- [ ] `taskify_users_registered_total` → should be 1
- [ ] `taskify_users_login_success_total` → should be 1
- [ ] `taskify_tasks_completed_total` → should be 1
- [ ] `taskify_appointments_created_total` → should be 1
- [ ] `rate(http_server_requests_seconds_count[5m])` → should show request rates
- [ ] `jvm_memory_used_bytes{area="heap"}` → should show memory usage

## Grafana Dashboard Verification

- [ ] Open: http://localhost:30301
- [ ] Login: admin / admin
- [ ] Navigate to: Dashboards → Browse
- [ ] Click: "Taskify Application Metrics"
- [ ] Verify panels show data:
  - [ ] HTTP Request Rate graph (should show activity)
  - [ ] JVM Heap Memory graph (should show memory usage)
  - [ ] Tasks Created counter (should show 1)
  - [ ] Tasks Completed counter (should show 1)

## Frontend UI Testing

- [ ] Open: http://localhost:30300
- [ ] Should see Taskify login page
- [ ] Click "Register" or navigate to register page
- [ ] Register with email: `ui-test@taskify.com`, password: `Password123!`
- [ ] Should redirect to dashboard
- [ ] Create a new task via UI
- [ ] Verify task appears in task list
- [ ] Mark task as complete
- [ ] Create an appointment via UI
- [ ] Verify appointment appears in appointments list
- [ ] Logout and login again
- [ ] Verify data persists (within same session - H2 is in-memory)

## Scaling Test

```bash
# Scale backend to 3 replicas
kubectl scale deployment taskify-backend -n taskify --replicas=3

# Watch pods scale
kubectl get pods -n taskify -w
# Should see new backend pod being created

# Verify 3 backend pods running
kubectl get pods -n taskify | grep taskify-backend
# Should show 3 pods in Running state

# Test that load is distributed
for i in {1..10}; do
  curl -s http://localhost:30800/actuator/health | jq .status
done
# All should return "UP"
```
- [ ] 3 backend pods running
- [ ] All health checks pass
- [ ] Metrics still accessible

**Scale back:**
```bash
kubectl scale deployment taskify-backend -n taskify --replicas=2
```

## Logs Verification

```bash
# Backend logs should show:
kubectl logs -n taskify -l app=taskify-backend --tail=20
```
- [ ] No ERROR messages
- [ ] Shows "Started TaskifyBackendApplication"
- [ ] Shows Actuator endpoints registered

```bash
# Prometheus logs should show:
kubectl logs -n taskify -l app=prometheus --tail=20
```
- [ ] No ERROR messages
- [ ] Shows "Server is ready to receive web requests"

## Cleanup Test

```bash
# Delete namespace
kubectl delete namespace taskify

# Verify deletion
kubectl get namespace taskify
# Should show: Error from server (NotFound)

# Stop Minikube
minikube stop
```
- [ ] Namespace deleted successfully
- [ ] Minikube stopped

## Final Checklist Summary

**Build & Package:**
- [ ] Backend builds successfully
- [ ] Frontend builds successfully
- [ ] Docker images created
- [ ] Images loaded into Minikube

**Deployment:**
- [ ] All 6 pods running
- [ ] All 4 services created
- [ ] Health checks passing

**Functionality:**
- [ ] User registration works
- [ ] User login works
- [ ] Task creation works
- [ ] Task completion works
- [ ] Appointment creation works

**Monitoring:**
- [ ] Prometheus scraping metrics
- [ ] All custom metrics visible
- [ ] Grafana dashboard showing data
- [ ] Grafana datasource connected

**DevOps Stack Complete:**
- [ ] ✅ CI/CD (Jenkins)
- [ ] ✅ Containerization (Docker)
- [ ] ✅ Orchestration (Kubernetes)
- [ ] ✅ Monitoring (Prometheus)
- [ ] ✅ Visualization (Grafana)

---

## 🎉 Congratulations!

If all checkboxes are marked, your complete DevOps pipeline is working perfectly!

**Your Achievement:**
- Full-stack application deployed on Kubernetes
- 2 replicas for high availability
- Comprehensive monitoring with 6 custom metrics
- Real-time dashboards with Grafana
- Production-ready architecture

**What You've Learned:**
- Spring Boot Actuator integration
- Custom Micrometer metrics
- Kubernetes deployments and services
- ConfigMaps and Secrets
- Prometheus configuration
- Grafana dashboard creation
- Health checks and readiness probes
- Horizontal scaling

Keep this checklist for future deployments! 🚀
