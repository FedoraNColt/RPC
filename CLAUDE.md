# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a custom RPC (Remote Procedure Call) framework built in Java 21 using Maven. The project implements a distributed communication system with advanced features like service discovery, load balancing, circuit breakers, rate limiting, and retry mechanisms.

## Project Structure

The project is organized as a multi-module Maven project with the following modules:

- **krpc-common**: Shared message classes, serializers, and utilities
- **krpc-core**: Core RPC functionality including client/server implementations
- **krpc-api**: Service interfaces and POJOs
- **krpc-provider**: Server-side test implementations
- **krpc-consumer**: Client-side test implementations

## Build Commands

```bash
# Build the entire project
mvn clean compile

# Run tests
mvn test

# Package the project
mvn clean package

# Install to local repository
mvn clean install
```

## Key Architecture Components

### Core Interfaces

- **RPCClient** (`krpc-core/src/main/java/org/example/client/rpcClient/RPCClient.java`): Main client interface for sending RPC requests
- **RPCServer** (`krpc-core/src/main/java/org/example/server/server/RPCServer.java`): Main server interface for handling RPC requests
- **ServiceCenter** (`krpc-core/src/main/java/org/example/client/serviceCenter/ServiceCenter.java`): Service discovery interface

### Message Structure

- **RPCRequest** (`krpc-common/src/main/java/org/example/common/message/RPCRequest.java`): Contains interface name, method name, parameters, and parameter types
- **RPCResponse** (`krpc-common/src/main/java/org/example/common/message/RPCResponse.java`): Contains response data and status

### Advanced Features

1. **Circuit Breaker** (`krpc-core/src/main/java/org/example/client/circuitBreaker/CircuitBreaker.java`):
   - Implements CLOSED/OPEN/HALF_OPEN states
   - Configurable failure threshold, success rate, and retry period
   - Automatic state transitions based on success/failure patterns

2. **Load Balancing** (`krpc-core/src/main/java/org/example/client/serviceCenter/balancer/`):
   - Multiple strategies: Random, Round Robin, Consistent Hashing
   - Dynamic node addition/removal support

3. **Rate Limiting** (`krpc-core/src/main/java/org/example/server/ratelimit/`):
   - Token bucket implementation for request throttling
   - Configurable rate limits per service

4. **Service Discovery**:
   - ZooKeeper-based service registration and discovery
   - Automatic service health monitoring

### Network Layer

- **Netty-based**: Uses Netty for high-performance async I/O
- **Multiple Transport Options**: Simple socket, Netty, and thread pool implementations
- **Custom Serialization**: JSON and object serialization support

## Testing

The project uses JUnit 4 for testing. Key test files:
- Circuit breaker tests: `krpc-core/src/test/java/org/example/client/circuitBreaker/CircuitBreakerTest.java`
- Load balancer tests: `krpc-core/src/test/java/balance/ConsistentHashingLoadBalancerTest.java`

## Dependencies

Key external dependencies:
- Netty 4.1.51.Final (networking)
- Apache Curator 5.1.0 (ZooKeeper client)
- Fastjson 1.2.83 (JSON serialization)
- Guava Retrying 2.0.0 (retry logic)
- Lombok 1.18.24 (code generation)
- SLF4J + Log4J (logging)

## Running Examples

- **Consumer Test**: `krpc-consumer/src/main/java/org/example/ConsumerTest.java` - Demonstrates concurrent RPC calls with thread pool
- **Provider Test**: `krpc-provider/src/main/java/org/example/TestServer.java` - Shows server setup and service registration

## Development Notes

- Project uses Java 21 features but has mixed compiler target configurations (some modules target Java 17)
- Lombok annotations extensively used for reducing boilerplate code
- All core components are interface-based for extensibility
- Comprehensive logging with SLF4J throughout the system