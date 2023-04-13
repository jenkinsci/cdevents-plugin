# Contributing

Contributions to the CDEvent plugins are encouraged. Please visit the [CDEvents](https://cdevents.dev/) site to help understand the purpose and concepts.

## Testing

Early adopters and testers are welcome to try out the plugin and provide feedback.

## Developing

### Prerequisites
- Java SDK 8: This plugin currently only support JDK8. For instructions on how to install, please refer to the [Installation Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
- CDEvents Java SDK: Currently this plugin is not available in the [Maven Central Repository](https://maven.apache.org/repository/) and needs to be compiled locally prior to building this plugin locally
 
#### Installing the CDEvents Java SDK
```shell
git clone https://github.com/cdevents/sdk-java.git
cd sdk-java; mvn clean -U install
cd ..; rm -rf sdk-java
```

### Testing the Plugin locally

1. Clone this repository locally
2. To start a local Jenkins instance, run the following:

   ```shell
   mvn clean hpi:run
   ```

3. To access your local instance, open a browser to http://localhost:8080/jenkins

## Issues

Please report any issues and any suggestions using the GitHub Issues in this repository. The issues will be reviewed and, if needed, we can help discuss ideas and solutions.
