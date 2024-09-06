
# Customs Reference Data

This service stores data from the EIS Reference Data Library in Mongo, for use in the NCTS frontend applications.

The data has a 14 day TTL (Time to Live), however the TTL can be disabled if needed (`mongodb.isTtlEnabled` in config) in the event of a prolonged issue with the ingestion process.

Note that for this change to take effect, the service will need to be redeployed with `mongodb.replaceIndexes` set to true.

There must be at least one snapshot of the reference data present at all times for the frontends to function. No data will result in an inability to populate dropdowns, radio buttons etc.

# Versioning

- v1.0 Header: application/vnd.hmrc.1.0+gzip (NCTS4)
- v2.0 Header: application/vnd.hmrc.2.0+gzip (NCTS5)

# v1.0

Version 1 stores reference data from EIS in a verbose state. This reference data is specific to transit and is intended for multiple consumers with a range of data requirements.

NOTE: Version 1 of this API will soon be deprecated and should NOT be used!

# v2.0

Version 2 stores reference data from DPS/EIS and is specific to NCTS5. This reference data has been crafted specifically to meet the NCTS5 requirements as laid out in the DDNTA. 

NOTE: Version 2 of this API is a tactical solution and will be replaced by a strategic pub/sub model, CRDL (Central Reference Data Library), at some point in the future.

Pub/Sub (or Publish/Subscribe) is an architectural design pattern used in distributed systems for asynchronous communication between different components or services. 
Although Publish/Subscribe is based on earlier design patterns like message queuing and event brokers, it is more flexible and scalable.

## Development
### Related test repositories
* [customs-reference-data-postman](https://github.com/hmrc/customs-reference-data-postman)
  * sample data for active development or testing of the service
  * test scenarios for the service
* [customs-reference-data-test-frontend](https://github.com/hmrc/customs-reference-data-test-frontend)
  * endpoints for ingesting data in localhost and the environments
  * endpoint for retrieving data in localhost and the environments
### Seeding service with data
- Follow the [instructions in the test repo](https://github.com/hmrc/customs-reference-data-test-frontend)

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
