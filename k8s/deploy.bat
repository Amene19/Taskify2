@echo off
echo =========================================
echo Deploying Taskify to Kubernetes
echo =========================================

echo Creating namespace...
kubectl apply -f namespace.yaml

echo Deploying backend...
kubectl apply -f backend\secret.yaml
kubectl apply -f backend\configmap.yaml
kubectl apply -f backend\deployment.yaml
kubectl apply -f backend\service.yaml

echo Deploying frontend...
kubectl apply -f frontend\configmap.yaml
kubectl apply -f frontend\deployment.yaml
kubectl apply -f frontend\service.yaml

echo Deploying Prometheus...
kubectl apply -f monitoring\prometheus\clusterrole.yaml
kubectl apply -f monitoring\prometheus\configmap.yaml
kubectl apply -f monitoring\prometheus\deployment.yaml
kubectl apply -f monitoring\prometheus\service.yaml

echo Deploying Grafana...
kubectl apply -f monitoring\grafana\configmap.yaml
kubectl apply -f monitoring\grafana\deployment.yaml
kubectl apply -f monitoring\grafana\service.yaml

echo.
echo =========================================
echo Deployment complete!
echo =========================================
echo.
echo Access URLs:
echo   Frontend:   http://localhost:30300
echo   Backend:    http://localhost:30800
echo   Prometheus: http://localhost:30900
echo   Grafana:    http://localhost:30301 (admin/admin)
echo.
echo Check status with:
echo   kubectl get pods -n taskify
echo   kubectl get services -n taskify
