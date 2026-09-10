# Spring Boot CI/CD and Observability Pipeline

An end-to-end delivery workflow for a Spring Boot event-management API. The project demonstrates how application tests, quality gates, artifact publishing, container delivery, deployment, and monitoring fit together in one Jenkins pipeline.

## Delivery flow

`GitHub push -> Maven build -> unit tests -> JaCoCo -> SonarQube quality gate -> Nexus -> Docker -> Docker Compose -> Prometheus/Grafana`

The pipeline fails when tests or the SonarQube quality gate fail. Successful builds publish the versioned JAR to Nexus, create and push a Docker image, deploy the application with Docker Compose, and expose application and container metrics.

## What is included

- Spring Boot 3 and Java 21 REST API
- Unit and controller tests using an embedded H2 database
- JaCoCo coverage reports and Jenkins test artifacts
- SonarQube analysis with enforced quality-gate status
- Versioned artifact publishing to Nexus
- Docker image build and Docker Compose deployment
- MySQL persistence
- Spring Boot Actuator and Prometheus metrics
- Provisioned Grafana dashboards
- cAdvisor and Node Exporter host/container monitoring

## Repository map

```text
Jenkinsfile                 CI/CD pipeline
Dockerfile                  application image
docker-compose.yml          application and monitoring stack
src/main/                   Spring Boot service
src/test/                   unit and controller tests
prometheus/                 scrape configuration
grafana/provisioning/       data source and dashboard provisioning
```

## Run the tests

Requirements: Java 21. The Maven wrapper is included.

```bash
./mvnw test
```

## Run the application stack

```bash
./mvnw clean package
docker build -t naderite/eventsproject:latest .
docker compose up -d
```

After startup:

- API: `http://localhost:8089`
- Swagger UI: `http://localhost:8089/swagger-ui.html`
- Prometheus: `http://localhost:9091`
- Grafana: `http://localhost:3000`

The Compose credentials are development defaults only. Replace them with secrets before using the stack outside a local demonstration environment.

## Jenkins configuration

Configure these Jenkins tools and credentials before running the pipeline:

- JDK tool named `JDK21`
- Maven tool named `Maven3`
- SonarQube secret text credential: `SonarQube_jenkins`
- Docker Hub username/password credential: `dockerhub-credentials`
- Reachable SonarQube and Nexus services, or updated `SONAR_HOST_URL` and `NEXUS_URL` values

This repository is an educational DevOps project. Its local defaults favor reproducibility and should be hardened before production use.
