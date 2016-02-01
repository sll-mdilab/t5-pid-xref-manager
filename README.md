# T5 FHIR Patient Identification Back-end (previously Patient Identification Proxy)

## Introduction

This is a server application which provides a FHIR interface for retrieving physiological observations enriched with patient ID information. It manages mappings between patient and device identifiers and retrieves observations based on those mappings. These mappings are represented by the DeviceUseStatement resource type. It also supports CRUD operations on a subset of the FHIR DSTU2 resources which are stored using FHIRbase in a Postgres database.

## Build

The application is written in Java version 8 and uses Gradle for automatic building and dependency management.
Assuming that Java EE 8 development kit is installed and exist on the PATH environment variable, the project can be built with the following command from the project root folder:

    ./gradlew build

This outputs a .war-file into the `build/lib` directory.

## Database
The application requires access to a Postgres >=v9.4 database with FHIRbase-plv8 >=v0.0.1-beta.4. Scripts for initializing the database schema can be found in the `t5-doc` project under the `sql`-folder.

## Deployment

The build process produces a servlet contained in a .war-file which can be deployed on any compatible Java servlet container. It has been tested with Apache Tomcat 8.0.

The application uses the following environment variables:

* `JDBC_CONNECTION_STRING` - A string containing connection information such as hostname/IP, username, password and other connection settings accepted by the Postgres JDBC driver. All application data including observation data and FHIR resources will be read from and/or stored in this database.
* `T5_XREF_API_KEY` - (Optional) An API key that should be required by all clients in order to access the service. An empty value means that the API key should not be verified.
* `T5_XREF_TIMESHIFT_START` - (Optional) Start of a master time interval which to use when fetching observations. For longer query intervals, observation from the master time interval will be repeated. Timestamps will be modified to make observations appear as they have occurred during the query interval.
* `T5_XREF_TIMESHIFT_END` - (Optional) End of the master time interval.
* `T5_XREF_PATIENT_IDS` - (Optional) A list of patiend IDs for which timeshift shall be applied. If empty, timeshif will be applied for all patient IDs.
* `T5_XREF_OAUTH_AUTHORIZE_URI`- The URI which clients should use for authorization. This will be included in the FHIR manifest.
* `T5_XREF_OAUTH_TOKEN_URI` - The URI which clients should use for obtaining an auth token from an auth code. This will be included in the FHIR manifest.
* `T5_XREF_OAUTH_INTROSPECTION_URL` - The URL to use for performing introspection (verification) of auth tokens.
* `T5_XREF_OAUTH_CLIENT_ID` - The client id which identifies this service.
* `T5_XREF_OAUTH_CLIENT_SECRET` - (Optional) The client secret to use when accessing the introspection endpoint.

## References
* FHIRbase https://github.com/fhirbase/fhirbase-plv8
