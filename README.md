## minimal-plugin-architecture-example ![Java 17](https://img.shields.io/badge/Java-17-green) ![Spring Boot 3.0.6](https://img.shields.io/badge/Spring--Boot-3.0.6-green)

This is a minimal plugin architecture project built with Spring Boot and Vue.js. It allows the main application to dynamically discover and load backend and frontend plugins at runtime.

The host application scans a designated directory for plugin JARs, loads their Spring controllers, and serves their compiled UI assets. Frontend plugins are built as ES modules and dynamically
imported by the Vue.js UI host application.

### Modules

| Module       | Role         | Description                                                                                       | Endpoint Path        | UI Path                                  |
|--------------|--------------|---------------------------------------------------------------------------------------------------|----------------------|------------------------------------------|
| api          | Interface    | Exposes the base Plugin interface that all plugins implement                                      | -                    | -                                        |
| app          | Host         | Spring Boot app that manages the lifecycle of plugins and dynamically registers their controllers | `/api/plugins/`      | -                                        |
| plugin-one   | Backend      | Exposes a simple REST endpoint via a plugin wrapper                                               | `/api/plugins/one`   | -                                        |
| plugin-two   | Fullstack    | Serves a standalone Vue application alongside backend APIs                                        | `/api/plugins/two`   | `/api/plugins/ui/plugin-two/index.html`  |
| plugin-three | UI Component | Compiles a Vue component into a library format to be imported by the UI Host                      | `/api/plugins/three` | -                                        |
| plugin-four  | UI Host      | A Vue application that fetches manifest data and mounts active plugins at runtime                 | `/api/plugins/four`  | `/api/plugins/ui/plugin-four/index.html` |

### Configuration

The `application.yml` file contains the configuration for the host application.

| Key         | Description                                                        | Example |
|-------------|--------------------------------------------------------------------|---------|
| server.port | Specifies the port on which the application will run               | 8080    | 
| plugin.dir  | Directory where the application scans for plugin JAR files to load | plugins |

### Deployment

#### Prerequisites

1. [Java Development Kit (JDK) 17](https://jdk.java.net/17/) - Download and install the JDK.
2. [Apache Maven](https://maven.apache.org/download.cgi) - Download and install the Maven build tool.

#### Deployment Steps

1. **Build the project**: Run `mvn clean package` in the root directory. Ensure the build step copied the plugin JARs into `app/target/plugins`.

2. **Run the application**: Navigate to the folder `app/target` and start the host application using `java -jar app-1.0-SNAPSHOT.jar`.

3. **Access the application**: You can now access the application's REST endpoints and UI using your preferred tools or web browser.
