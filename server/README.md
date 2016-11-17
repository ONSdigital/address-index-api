# Server Routes #

```
#Highest Priority First

GET     /                   uk.gov.ons.addressIndex.server.controllers.general.ApplicationController.index
#EG     /addresses?format=myFormat&input=myInputString
GET     /addresses          uk.gov.ons.addressIndex.server.controllers.AddressController.addressQuery(input, format)
#EG     /addresses/myUprn?format=myFormat
GET     /addresses/:uprn    uk.gov.ons.addressIndex.server.controllers.AddressController.uprnQuery(uprn, format)
GET     /es                 uk.gov.ons.addressIndex.server.controllers.AddressController.elasticTest
#swagger play
#GET    /api                controllers.ApiHelpController.getResources
```