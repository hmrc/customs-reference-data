
# Customs Reference Data

This service stores data from the EIS Reference Data Library in Mongo, for use in the NCTS frontend applications.

For any given feed (`RefDataFeed` or `ColDataFeed`), data older than 14 days is deleted upon the successful ingestion of a new snapshot of data.

There must be at least one snapshot of reference data present at all times (for both data feeds) for the frontends to function. No data will result in an inability to populate dropdowns, radio buttons, etc.

## Versioning

### Requests from DPS

#### v1.0

This has been deprecated and decommissioned

#### v2.0

Version 2 stores reference data from DPS/EIS and is specific to NCTS5. This reference data has been crafted specifically to meet the NCTS5 requirements as laid out in the DDNTA. 

NOTE: Version 2 of this API is a tactical solution and will be replaced by a strategic pub/sub model, CRDL (Central Reference Data Library), at some point in the future.

Pub/Sub (or Publish/Subscribe) is an architectural design pattern used in distributed systems for asynchronous communication between different components or services. 
Although Publish/Subscribe is based on earlier design patterns like message queuing and event brokers, it is more flexible and scalable.

### Requests from NCTS frontends

#### v1.0

This corresponds to P5 code lists:
* Requests will retrieve data from MongoDB
* Test-only requests will retrieve data from [here](conf/resources/phase-5)

#### v2.0

This corresponds to P6 code lists:
* Requests will retrieve data from [crdl-cache](https://github.com/hmrc/crdl-cache)
* Test-only requests will retrieve data from [here](conf/resources/phase-6)

## Endpoints

---

### `POST /reference-data-lists`

#### Params

##### Headers
* Content-Type - `application/json`
* Content-Encoding - `gzip`
* Accept - `application/vnd.hmrc.2.0+gzip`
* Authorization - `Bearer <token>` (see `incomingRequestAuth.acceptedTokens` in the relevant environment)

##### Body
* JSON data in `.json.gz` (GZIP) format, conformant with [CTCUP06](/conf/schemas/CTCUP06.schema.json) schema. A sample payload can be found [here](/conf/resources/csrd2-payloads/RD_NCTS-P5.json.gz).

#### Successful response

##### 202 ACCEPTED

* The data has been successfully ingested and stored in mongo.

#### Unsuccessful responses (with possible causes)

##### 400 BAD_REQUEST
* The data is in the correct GZIP format but the underlying data does not conform with the schema
* Missing or incorrect `Content-Encoding` header (e.g. `Invalid Json: Illegal character`)
* Missing or incorrect `Accept` header

##### 401 UNAUTHORIZED
* Invalid or missing bearer token in the Authorization header (`Supplied Bearer token is invalid`)

##### 415 UNSUPPORTED MEDIA TYPE
* Missing or incorrect `Content-Type` header (`Expecting text/json or application/json body`)

##### 500 INTERNAL SERVER ERROR
* The data was in the wrong format (e.g. `java.util.zip.ZipException: Not in GZIP format`)
* An error occurred in the mongo client

---

### `POST /customs-office-lists`

#### Params

* Headers
  * Content-Type - `application/json`
  * Content-Encoding - `gzip`
  * Accept - `application/vnd.hmrc.2.0+gzip`
  * Authorization - `Bearer <token>` (see `incomingRequestAuth.acceptedTokens` in the relevant environment)
* Body
  * JSON data in `.json.gz` (GZIP) format, conformant with [CTCUP08](/conf/schemas/CTCUP08.schema.json) schema. A sample payload can be found [here](/conf/resources/csrd2-payloads/COL-Generic-20250212.json.gz).

#### Successful response

##### 202 ACCEPTED

* The data has been successfully ingested and stored in mongo.

#### Unsuccessful responses (with possible causes)

##### 400 BAD_REQUEST
* The data is in the correct GZIP format but the underlying data does not conform with the schema
* Missing or incorrect `Content-Encoding` header (e.g. `Invalid Json: Illegal character`)
* Missing or incorrect `Accept` header

##### 401 UNAUTHORIZED
* Invalid or missing bearer token in the Authorization header (`Supplied Bearer token is invalid`)

##### 415 UNSUPPORTED MEDIA TYPE
* Missing or incorrect `Content-Type` header (`Expecting text/json or application/json body`)

##### 500 INTERNAL SERVER ERROR
* The data was in the wrong format (e.g. `java.util.zip.ZipException: Not in GZIP format`)
* An error occurred in the mongo client

---

### `GET /lists/:listName`

#### Phase 5

##### Params

* Headers
  * Accept - `application/vnd.hmrc.1.0+json`
* Endpoint
  * For a valid `listName` see the code lists in the schemas
* Query
  * To filter the response based on a certain field (e.g. `/lists/CountryCodesFullList?data.code=GB`)

##### Successful response

###### 200 OK

* The response JSON contains the desired data

```
{
    "_links": {
        "self": {
            "href": "/customs-reference-data/lists/CountryCodesFullList?data.code=GB"
        }
    },
    "meta": {
        "version": "b4c0d7d6-a172-46ce-b461-e8a5b34c93b2",
        "snapshotDate": "2025-02-12"
    },
    "id": "CountryCodesFullList",
    "data": [
        {
            "state": "valid",
            "activeFrom": "2024-02-21",
            "code": "GB",
            "description": "United Kingdom"
        }
    ]
}
```

##### Unsuccessful responses (with possible causes)

###### 404 NOT FOUND
* The `listName` was not found

###### 500 INTERNAL SERVER ERROR
* An error occurred in the mongo client

#### Phase 6

##### Params

* Headers
  * Accept - `application/vnd.hmrc.2.0+json`
* Endpoint
  * For a valid `listName` see the code lists in the schemas
* Query
  * To filter the response based on a certain field (e.g. `/lists/CountryCodesFullList?keys=GB`)

##### Successful response

###### 200 OK

* The response JSON contains the desired data

```
[
    {
      "key": "GB",
      "value": "United Kingdom",
      "properties": {
        "state": "valid"
      }
    }
]
```

##### Unsuccessful responses (with possible causes)

###### 400 NOT FOUND
* The `listName` was not valid

###### 500 INTERNAL SERVER ERROR
* An error occurred retrieving the data from crdl-cache

## Development
### Related test repositories
* [customs-reference-data-postman](https://github.com/hmrc/customs-reference-data-postman)
  * sample data for active development or testing of the service
  * test scenarios for the service
* [customs-reference-data-test-frontend](https://github.com/hmrc/customs-reference-data-test-frontend)
  * endpoints for ingesting data in localhost and the environments
  * endpoint for retrieving data in localhost and the environments

### Seeding service with data
#### Postman
Follow the [instructions in the test repo](https://github.com/hmrc/customs-reference-data-test-frontend).

#### Curl
Ingestion can also be achieved through curl. DPS and EIS colleagues may use curl to test the ingestion of the data.
The request will look something like the following.
The `<url>`, `<token>` and `<filename>` values can be populated as appropriate.
```
curl -kv -X POST <url>
-H 'Accept: application/vnd.hmrc.2.0+gzip'
-H 'Content-Encoding: gzip'
-H 'Content-Type: application/json'
-H 'Authorization: Bearer <token>'
--data-binary '@<filename>.json.gz'
```

## Testing

Run unit tests:
<pre>sbt test</pre>
Run integration tests:
<pre>sbt it/test</pre>

## Running manually

<pre>
sm2 --start CTC_TRADERS_P5_ACCEPTANCE
sm2 --stop CUSTOMS_REFERENCE_DATA
sbt -Dplay.http.router.router=testOnlyDoNotUseInAppConf.Routes run
</pre>

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
