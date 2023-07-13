
# Customs Reference Data

This service stores data from the EIS Reference Data Library in Mongo.

# Versioning

- v1.0 Header: application/vnd.hmrc.1.0+gzip (DEPRECATED!!!)
- v2.0 Header: application/vnd.hmrc.2.0+gzip (MARKED FOR DEPRECATION)

# v1.0

Version 1 stores reference data from EIS in a verbose state. This reference data is specific to transit and is intended for multiple consumers with a range of data requirements.

NOTE: Version 1 of this API will soon be deprecated and should NOT be used!

# v2.0

Version 2 stores reference data from DPS/EIS and is specific to NCTS5. This reference data has been crafted specifically to meet the NCTS5 requirements as laid out in the DDNTA. 

NOTE: Version 2 of this API is a tactical solution and will be replaced by a strategic pub/sub model later this year.

_Pub/Sub (or Publish/Subscribe) is an architectural design pattern used in distributed systems for asynchronous communication between different components or services. 
Although Publish/Subscribe is based on earlier design patterns like message queuing and event brokers, it is more flexible and scalable._

## Related test repositories
This [GitHub repo](https://github.com/hmrc/customs-reference-data-postman#working-with-the-collection-via-command-line-interface) has:
- sample data for active development or testing of the service
- test scenarios for the service

## Development
### Seeding service with data
- Follow the [instructions in the test repo](https://github.com/hmrc/customs-reference-data-postman#working-with-the-collection-via-command-line-interface)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
