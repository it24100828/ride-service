# RideLink - Ride Management Service (Member 3)

## Responsibility
Ride request, driver assignment (calls Driver Service), lifecycle states, fare estimate integration.

## Run
```bash
./mvnw spring-boot:run
```
Swagger: http://localhost:8083/swagger-ui.html

## Key Endpoints
- POST /api/rides
- POST /api/rides/{id}/assign?serviceArea=Colombo
- PUT  /api/rides/{id}/status
- GET  /api/rides/{id}

## Interservice calls
1. Fare Service → estimate on create
2. Driver Service → available drivers on assign
