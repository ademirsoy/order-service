# Photo Shooting Order Service
Order Service is an application where you can book an order for a photo shooting service 
and manage its evolution.
- It stores data in internal memory for simplicity, and the data is lost on restart.
- If you want to run the application on a different port you can edit `server.port=8080` in the `src/main/resources/application.properties` file

## Usage

### Build (Skip to `Run With Docker` if JDK 11 not exists)
- It's a Java project that requires Jdk 11 or higher

- Run the following script in root directory in order to build with Maven:

  **`mvn clean install`**
- Run the following if Maven is not installed

  **`./mvnw clean install`**

  or in Windows

  **`./mvnw.cmd clean install`**

### Run
- Once the build is completed, you can run with one of the following commands:

  **`mvn spring-boot:run`**

  **`java -jar target/photo-shooting-order-service-0.0.1.jar`**

  **`./mvnw spring-boot:run`**

  **`./mvnw.cmd spring-boot:run`**


### RUN WITH DOCKER
**`docker run -p 8080:8080 alidemirsoy/photo-shooting-order-service`**

### TEST
You can easily test the application by sending HTTP requests via cURL or any other Http client.
Note that you need to set Content-Type header to application/json
- An example cURL request for creating an order is as follows:
  - `curl  --request POST 'localhost:8080/orders' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "contact": {
        "name": "marco",
        "surname": "verratti",
        "email": "marco@polo.com",
        "cellNumber": "1231232"
      },
      "photoType": "Food",
      "date": "2021-06-30",
      "time": "10:30:00",
      "timezone" : "Europe/Istanbul"
      }'`
- An example cURL request for scheduling an order is as follows:
  - `curl --location --request PATCH 'localhost:8080/orders/1/schedule' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "date": "2021-07-05",
      "time": "12:00:00"
    }'`
- Here's a compete list of available endpoints
  - GET /orders
  - POST /orders
  - PATCH /orders/{id}/schedule 
    - Request body: `{"date": "2021-07-05", "time": "12:00:00", "timezone": "Europe/Berlin"}`
  - PATCH /orders/{id}/assign
    - Request body: `{"photographerId": 10}`
  - PATCH /orders/{id}/complete
  - PATCH /orders/{id}/reject-photos
  - PATCH /orders/{id}/cancel
  - POST /orders/{id}/upload
    - Request header: `Content-Type: multipart/form-data`
  - POST /photographers
    - Request body: `{"name": "Immobile" }`
  - POST /photographers/bulk
    - Request body: `[{"name": "Immobile" }, {"name": "Bonucci" }]`

### ASSUMPTIONS
- Operators can see the photos to verify, through an external tool
- Default order duration is assumed to be 1 hour and can be modified via `durationInHours` field in Order object
- Default timezone of the orders is assumed to be system default timezone if not specified. 
  Ideally clients need to send `timezone` field when creating or scheduling orders.
- Supported timezone inputs are in following formats: `Europe/Berlin`, `GMT+3`, `UTC`
- Business hours for a valid order is assumed to be according client's local time.
- Photographer is assumed to have only `name` attribute
- A photographer can be assigned to only one order at a time interval.
- Orders can only have one assigned photographer.
- Maximum allowed file size for uploading zip files in 50MB and can be adjusted in `src/main/resources/application.properties`

### Monitoring
- The logs of the program is located under `logs` directory
- There's a health endpoint to monitor the status of the application: `http://localhost:8080/actuator/health`
