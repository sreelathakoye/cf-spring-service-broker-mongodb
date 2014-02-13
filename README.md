cloudfoundry-service-broker
===========================

Spring MVC based REST application for V2 CloudFoundry service brokers based on a suggestion by Steven Greenberg at https://github.com/cloudfoundry-community/spring-service-broker.

The enhancements are:

- Migration to the latest Spring Framework 4.0.1
- Added default services

# Overview

The goal is to provide a boilerplate Spring MVC application that can be used to quickly implement new Service Brokers in CloudFoundry.  The boilerplate implements the restful controllers required of service brokers and provides a set of 3 simple interfaces to implement for a new service.  

## Compatibility

- cf-release-151 on bosh-lite.
- Service Broker API: v2.1

## Getting Started

To use:

1. Fork the project
2. Implement (3) interfaces in the com.pivotal.cf.broker.service package (alternatively, you can use the included BeanCatalogService and just implement the other (2) interfaces)
3. Ensure your service impls are annotated with @Service 
4. Build the project and run the tests: `gradle build`
5. Push the broker to CloudFoundry as an app: `cf push <your-broker> --path build/libs/<war>`
6. Register your service broker with CF: `cf add-service-broker <service-broker-name>`

### Security

When you register your broker with the cloud controller, you are prompted to enter a username and password.  This is used by the broker to verify requests.

By default, the broker uses Spring Security to protect access to resources.  The username and password are stored in: /src/main/webapp/WEB-INF/spring/security-context.xml".  By default, the password should be encoded using the Spring BCryptPasswordEncoder.  A utility class is included to provide encryption.  You can encrypt the password executing: 

`java com.pivotal.cf.broker.util.PasswordEncoder password-to-encrypt`

### Testing

Integration tests are included to test the controllers.  You are responsible for testing your service implementation.  

- Initial draft of RestTemplate endpoint tests.

### Model Notes

- The model is for the REST/Controller level.  It can be extended as needed.
- All models explicitly define serialization field names.

## To Do

* More integration testing around expected data input and output
* Version headers
* Integrate w/ NATS to allow this war to be deployed with Bosh
* Create a Bosh release
* Separate integration project to test broker endpoints
* Migrate to Spring Web Configuration



