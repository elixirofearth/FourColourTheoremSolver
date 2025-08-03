# 🗺️ Four-Colour Map Theorem Solver 🎨

## 🌟 Summary

Welcome to the Four-Colour Map Theorem Solver, an interactive web application demonstrating one of mathematics's most significant theorems in graph theory and topology. This tool provides a practical implementation of the Four-Colour Theorem, which states that any planar map can be coloured using no more than four colours while ensuring that no adjacent regions share the same colour, through an intuitive and user-friendly interface.

Our implementation allows users to:

- Create custom maps through an interactive canvas
- Automatically generate mathematically valid four-colour solutions
- Save and download colored maps
- Visualize the theorem's practical applications



https://github.com/user-attachments/assets/964ce3f2-f9cd-4709-933e-80e75afa7fd3



## 🏗️ Architecture

### Frontend (React + TypeScript)

- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite
- **State Management**: Redux Toolkit with Redux Persist
- **Styling**: Tailwind CSS
- **Testing**: Vitest + React Testing Library + Playwright E2E
- **HTTP Client**: Fetch API
- **Authentication**: JWT tokens

### Backend (Spring Boot Microservices)

- **API Gateway** (REST) - Main entry point, routing, authentication, Redis caching
- **Authentication Service** (REST) - User auth, JWT tokens, PostgreSQL
- **Map Storage Service** (REST) - CRUD operations, MongoDB
- **Logger Service** (gRPC & AMQP) - Centralized logging, Kafka messaging
- **Solver Service** (REST) - Python Flask service for graph coloring algorithms
- **Testing**: JUnit5 + Mockito + Spring Boot Test

### Infrastructure

- **PostgreSQL**: User data and sessions
- **MongoDB**: Map storage and logs
- **Redis**: Caching and session storage
- **Kafka**: Message queuing for logs
- **Zookeeper**: Kafka coordination

### DevOps & Deployment

- **Frontend**: Vercel deployment
- **Containerization**: Docker images on Docker Hub
- **Orchestration**: Kubernetes on Vultr Kubernetes Engine
- **CI/CD**: GitHub Actions with automated testing and deployment
- **Networking**: NGINX Ingress Controller with single public IP

## 🧮 Map Coloring Algorithm

The solver implements a sophisticated constraint satisfaction problem (CSP) approach for the Four-Colour Theorem:

### Algorithm Overview

1. **Image Processing**: Converts user-drawn maps to binary representation
2. **Region Detection**: Uses flood-fill algorithms to identify distinct regions
3. **Adjacency Detection**: Determines which regions border each other using morphological operations
4. **Graph Construction**: Creates a graph where vertices represent regions and edges represent adjacencies
5. **Constraint Satisfaction**: Solves the coloring problem using advanced CSP techniques

### CSP Implementation Details

#### Core Components:

- **Variable Selection**: Minimum Remaining Values (MRV) heuristic
- **Value Ordering**: Least Constraining Value (LCV) heuristic
- **Constraint Propagation**: Forward checking to reduce search space
- **Backtracking**: Systematic search with intelligent backtracking

#### Algorithm Features:

- **Heuristic Optimization**: MRV and LCV heuristics for efficient search
- **Forward Checking**: Prevents invalid assignments early
- **Fallback Strategy**: Greedy coloring if CSP fails
- **Performance Monitoring**: Tracks processing time and complexity

#### Mathematical Foundation:

The algorithm ensures that:

- No adjacent regions share the same color
- Only four colors are used (red, green, blue, yellow)
- The solution is mathematically valid according to the Four-Colour Theorem
- The coloring is optimal in terms of color usage

## 📞 Contact

- Andy Tran ([anhquoctran006@gmail.com](mailto:anhquoctran006@gmail.com))
- Riley Kinahan ([rdkinaha@ualberta.ca](mailto:rdkinaha@ualberta.ca))
