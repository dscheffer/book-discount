# Book Discount App



## System Requirements
* Java 21+
* Docker

## Build

To build the entire web app including frontend and unit as well as integration tests run the following command:
```
./mvnw clean install
```

## Create Docker container

After running the build command you can create a docker image using:
```
./mvnw jib:dockerBuild
```

## Running the application via Docker

To run the resulting image you can use this project's docker compose file.
`compose.yaml` is located in the project root and contains the app pre-configured with a postgres database.
To get an operational web application simply run:
```
docker compose -up -d
```
The application will then be available on `http://localhost:9081`

## Local development

The React dev server can be started from `book-discount-ui/src/main/js` by running
```
npm run start
```

After starting the dev server as long as `BookDiscountApplication` is running from the default port in the IDE, the react ui will be operational with rapid turnaround on `http://localhost:3000`