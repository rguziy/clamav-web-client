# ClamAV Web UI Client

## Overview
Welcome to the ClamAV Web UI Client! This Spring Boot MVC application provides a user-friendly web interface for the popular ClamAV antivirus service, allowing users to easily manage and interact with the ClamAV back-end for efficient virus and malware scanning.

## Features
- **User-Friendly Interface**: Clean and simple design for easy navigation and use.
- **Real-Time Scanning**: Initiate scans instantly from the web UI and view real-time results.
- **File Upload**: Easily upload files for scanning directly from the web interface.
- **Scan Folder**: Easily scan and analyze all files within a specified folder, as defined via the web interface.
- **Custom Configuration**: Adjust ClamAV settings and configurations through the UI.
- **Multi-User Support**: Allow multiple users to access and use the application.
- **Multi-Language Support**: Enables users to interact with the application in their preferred language, enhancing accessibility and user experience.
- **Responsive Design**: Optimized for both desktop and mobile devices.

## Running the Application

### Run with Maven
If you prefer to run the application directly without Docker, you can do so using Maven:

1. #### Clone the Repository
```bash
git clone https://github.com/rguziy/clamav-web-client.git
```

2. #### Navigate to the Project Directory
```bash
cd clamav-web-client
```

3. ####  Build the Application
```maven
mvn clean package
```

4. #### Run the Application Use the following command to run the application:
```maven
mvn spring-boot:run
```
The application will start and be accessible at ``` http://localhost:8080 ```.

5. #### Environment Variables 
You can set environment variables by defining them in your command line before running the application:
```bash
export CLAMAV_HOST=clamav-server
export CLAMAV_PORT=3310
```

### Run with Docker
The application can be easily deployed and managed in a Docker container:

#### Docker Image
You can pull the pre-built Docker image from Docker Hub:
```bash
docker pull yourusername/clamav-web-ui
```
Alternatively, you can build the image yourself by cloning this repository and running:
```bash
docker build -t clamav-web-ui .
```

#### Running the Container
To run the container, use the following command:
```bash
docker run -p 8080:8080 clamav-web-ui
```
This will start the application and make it available at ``` http://localhost:8080 ```

#### Environment Variables
You can configure the application by setting environment variables such as:

``` CLAMAV_HOST ```: the hostname or IP address of the ClamAV server  
``` CLAMAV_PORT ```: the port number of the ClamAV server

For example:
```bash
docker run -p 8080:8080 -e CLAMAV_HOST=clamav-server -e CLAMAV_PORT=3310 clamav-web-ui
```

#### Docker Compose
Alternatively, you can use Docker Compose to run the application with a ClamAV server:
```docker
version: '3'
services:
  clamav-web-ui:
    image: yourusername/clamav-web-ui
    ports:
      - "8080:8080"
    environment:
      - CLAMAV_HOST=clamav-server
      - CLAMAV_PORT=3310
    depends_on:
      - clamav-server

  clamav-server:
    image: clamav/clamav
    ports:
      - "3310:3310"
```
Run ``` docker-compose up ``` to start both containers.


## License
This project is licensed under the GNU Lesser General Public License version 2.1.
You can find the text of the license in the `LICENSE` file in the root directory of this repository.

## Acknowledgements
- ClamAV team for creating the antivirus service ``` https://www.clamav.net ```.
- Clement Darras for creating Java ClamAV Client Library ``` https://github.com/cdarras/clamav-client ``` .
- Spring Boot team for creating the Spring Boot framework ``` https://spring.io/projects/spring-boot ```.
- Bootstrap team for creating a powerful, feature-packed frontend toolkit ``` https://getbootstrap.com ```.
