# GarageSale
## Overview
This project was developed in order to get hands-on experience instrumenting a Java Spring Boot application using the OpenTelemetry SDK by hand, as well as instrumenting with an Enterprise-class APM Platform (AppDynamics).

There is no guarantee that this application is built to any best practices or standards, and in certain cases is explicitly designed to **not** be performant, and so from the angle of tracing and monitoring, it's all good.

It's not necessary to build this project.  All images can be pulled from Docker Hub when you run with `docker-compose up -d`.

## Quick Start
### Prerequisites
In order to run this project, you'll need:
- Docker
- Docker Compose
   > __Note:__  The Docker versions must support Docker Compose File version 3.2+

### Steps to Run
1. Clone this repository to your local machine.
2. Configure the `.env` file in the root project directory.

   > __IMPORTANT:__ Detailed information regarding `.env` file can be found [below](###-.env-File).  This __MUST__ be done for this project to work!
3. Use Docker Compose to start
```bash
$ docker-compose up -d
```

## Build
__Note:__ the build process requires internet access.
### Prerequisites
If you'd like to build the project locally, you'll need:
- Java 1.8+
- Maven 3.x
- Docker
- Docker Compose

### Steps to Build
1. Clone this repository to your local machine.
2. For each `ui` and `item-api`, run build script.
```bash
# Move into app directory
$ cd ui
# Run the maven build and place runnable jar in docker directory
$ ./buildForDocker.sh
```
3. Configure the `.env` file in the root project directory.

   > __IMPORTANT:__ Detailed information regarding `.env` file can be found [below](###-.env-File).  This __MUST__ be done for this project to work!
4. Use Docker Compose to build local images
```bash
$ docker-compose build
```
5. Use Docker Compose to start
```bash
$ docker-compose up -d
```

## More Notes on Configuration
### Project File Structure
Abbreviated tree output (only relevant files & paths shown)
```bash
$ GarageSale
├── README.md
├── docker-compose.yml
├── .env
├── item-api
│   ├── buildForDocker.sh
│   ├── docker
│   │   ├── Dockerfile
│   │   ├── downloadJavaAgentLatest.sh
│   ├── imageBuildAndRunTailLog.sh
│   ├── pom.xml
│   ├── runWithBuild.sh
│   ├── src
│   │   ├── main
│   │   └── test
└── ui
    ├── buildForDocker.sh
    ├── docker
    │   ├── Dockerfile
    │   ├── downloadJavaAgentLatest.sh
    │   ├── images
    ├── imageBuildAndRunTailLog.sh
    ├── pom.xml
    ├── runWithBuild.sh
    ├── src
    │   ├── main
    │   └── test
```
### Application Code
The app code is housed in `ui` and `item-api` directories.  Each directory contains source code and a `docker` directory.  The `docker` directory contains:
- `Dockerfile`
- `downloadJavaAgentLatest.sh`
   - A script to download the latest AppDynamics Java Agent.

### docker-compose.yml
This file is located in the project root and manages building and running the Docker containers. It uses the `.env` file to populate environment variables for the project to work properly.

### .env File
This file contains all of the environment variables that need to be populated in order for the project to run, and for the performance tools to operate.  Items that *must* be tailored to your environment are:

```bash
# AppD Java Agent
APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY=78df80d5-f501-4f94-bafd-d04c78b057be
APPDYNAMICS_AGENT_ACCOUNT_NAME=customer1
APPDYNAMICS_CONTROLLER_HOST_NAME=192.168.86.40
APPDYNAMICS_CONTROLLER_PORT=8090
APPDYNAMICS_CONTROLLER_SSL_ENABLED=false
```
> __Tip:__  Documentation on these configuration properties can be found in the [AppDynamics Java Agent Configuration Documentation](https://docs.appdynamics.com/display/PRO45/Java+Agent+Configuration+Properties)

```bash
# AppD Browser EUM
APPDYNAMICS_BROWSER_EUM_APPKEY=AA-AAA-AAA-AAA
APPDYNAMICS_BROWSER_EUM_ADRUM_EXT_URL_HTTP=http://cdn.appdynamics.com
APPDYNAMICS_BROWSER_EUM_ADRUM_EXT_URL_HTTPS=https://cdn.appdynamics.com
APPDYNAMICS_BROWSER_EUM_BEACON_HTTP=http://col.eum-appdynamics.com
APPDYNAMICS_BROWSER_EUM_BEACON_HTTPS=https://col.eum-appdynamics.com
```
> __Tip:__  Documentation on these configuration properties can be found in the [AppDynamics Real User Monitoring Documentation](https://docs.appdynamics.com/display/PRO45/Set+Up+and+Access+Browser+RUM)

**The rest of the environment variables can be left with default values.**

## Development/Testing
This repo contains some artifacts to ease re-builds for testing.
- `runWithBuild.sh`
   - This script builds a runnable jar and runs it locally (no container)
- `imageBuildAndRunTailLog.sh`
   - This script builds a runnable jar, builds the app image, creates (or re-creates) the container, and tails the container logs - very useful for debugging a recent code change
