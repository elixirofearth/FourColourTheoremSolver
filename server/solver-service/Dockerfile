# Use Python 3.11.5 as base image
FROM python:3.11.5-slim

# Set working directory
WORKDIR /app

# Install system dependencies required for scientific packages and protobuf compiler
RUN apt-get update && apt-get install -y \
    build-essential \
    protobuf-compiler \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first to leverage Docker cache
COPY solver-service/requirements.txt .

# Install Python dependencies
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Create necessary directories
RUN mkdir -p proto/logs

# Copy proto files first
COPY solver-service/proto/logs/logger.proto proto/logs/

# Create __init__.py files to make proto a proper Python package
RUN touch proto/__init__.py proto/logs/__init__.py

# Generate Python code from proto files
RUN python -m grpc_tools.protoc \
    -I. \
    --python_out=. \
    --grpc_python_out=. \
    proto/logs/logger.proto

# Copy the rest of the application code
COPY solver-service .

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV PORT=8082

# Expose the ports the app runs on
EXPOSE ${PORT}

ENV PYTHONPATH=/app

# Command to run the application
CMD ["sh", "-c", "gunicorn --bind 0.0.0.0:${PORT} app:app"]