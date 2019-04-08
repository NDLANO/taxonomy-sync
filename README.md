# NDLA Taxonomy Sync

Tips & tricks:
- Set up run conf to:
```
SERVER_SERVLET_CONTEXT_PATH=/api
TARGET_SERVER=localhost:5000 #<-- Endre til target
SOURCE_USER=sa
SOURCE_SERVER=localhost:5000 #<-- Endre til source
TARGET_DB=taxonomy
SOURCE_PW=null #<-- Endre til source pw
SOURCE_DB=taxonomy
TARGET_PW=null #<-- Endre til target pw 
TARGET_USER=sa
```

Last ned DynamoDB docker fra https://hub.docker.com/r/amazon/dynamodb-local/

`docker pull amazon/dynamodb-local`

Kjør DynamoDB docker lokalt på port 8000:

`docker run -p 8000:8000 amazon/dynamodb-local`

IntelliJ -> Settings -> Build, Execution, Deployment -> Build tools -> Gradle -> Runner:

`Check - 'Delegate IDE build/run actions to gradle'`


# Running with taxonomy-API

This project is intended to run as an application that sits between two instances of taxonomy-api. One instance of the 
taxonomy-api service would be running in draft mode, and the other would be a production service. 