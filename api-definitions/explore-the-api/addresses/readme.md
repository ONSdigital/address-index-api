<h1>/addresses</h1>

<p>Search for an address.</p>

<h2>Request</h2>

<p><code>GET /addresses</code></p>
 

<h3>Query parameters</h3>

<table class="table">
        <thead class="table--head">
        <th scope="col" class="table--header--cell">Parameter name</th>
        <th scope="col" class="table--header--cell">Value</th>
        <th scope="col" class="table--header--cell">Description</th>
        <th scope="col" class="table--header--cell">Additional</th>
        </thead>
    <tbody>
        <tr class="table--row">
            <td class="table--cell">input</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the address search string (e.g. &#39;14 Acacia Avenue, Ruislip, HA4 8RG&#39;).</td>
            <td class="table--cell">
                Required
             </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">offset</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the offset from zero, used for pagination.</td>
            <td class="table--cell">
                Optional
                <br>Default: 0
                <br>Maximum: 250
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">limit</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the number of addresses to return.</td>
            <td class="table--cell">
                Optional
                <br>Default: 10
                <br>Maximum: 100
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">classificationfilter</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential, commercial, workplace or educational</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">rangekm</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Limit results to those within this number of kilometers of point (decimal e.g. 0.1)</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
          <tr class="table--row">
            <td class="table--cell">lat</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Latitude of point in decimal format (e.g. 50.705948).</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">lon</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Longitude of point in decimal format (e.g. -3.5091076).</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">historical</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Include historical addresses</td>
            <td class="table--cell">
                Optional
                <br>Default: True
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">matchthreshold</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Minimum confidence score (percentage) for match to be included in results.</td>
            <td class="table--cell">
                Optional
                <br>Default: 5
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">verbose</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Include the full address details in the response (including relatives, crossRefs, paf and nag).</td>
            <td class="table--cell">
                Optional
                <br>Default: False
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">epoch</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Select a specific AddressBase Epoch to search.</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
            <tr class="table--row">
               <td class="table--cell">eboost</td>
               <td class="table--cell">string</td>
               <td class="table--cell">Set to 0 to exclude addresses in England</td>
               <td class="table--cell">
                    Optional
                    <br>Default: 1.0
                </td>
            </tr>
            <tr class="table--row">
                <td class="table--cell">nboost</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Set to 0 to exclude addresses in Northern Ireland</td>
                <td class="table--cell">
                    Optional
                    <br>Default: 1.0
                </td>
            </tr>
            <tr class="table--row">
                <td class="table--cell">sboost</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Set to 0 to exclude addresses in Scotland</td>
                <td class="table--cell">
                    Optional
                    <br>Default: 1.0
                </td>
            </tr>
            <tr class="table--row">
                <td class="table--cell">wboost</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Set to 0 to exclude addresses in Wales</td>
                <td class="table--cell">
                     Optional
                    <br>Default: 1.0
                </td>
            </tr> 
            <tr class="table--row">
                <td class="table--cell">pafdefault</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Set to true to use PAF instead of NAG as default formatted address where possible</td>
                <td class="table--cell">
                     Optional
                    <br>Default: false
                </td>
            </tr> 
     </tbody>
  </table>
    

<h2>Responses</h2>

<h3>200</h3>
<p>Success. A json return of matched addresses.</p>

<h3>429</h3>
<p>Server too busy. The Address Index API is experiencing exceptional load.</p>

<h3>500</h3>
<p>Internal server error. Failed to process the request due to an internal error.</p>

<h3>400</h3>
<p>Bad request. Indicates an issue with the request. Further details are provided in the response.</p>

<h3>401</h3>
<p>Unauthorised. The API key provided with the request is invalid.</p>
    

   <h2>CURL example</h2>

   <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses</code></pre>

<h2>Sample Queries</h2>

<p><pre>7 Gate Reach, Exeter</pre></p>
<p><pre>University Of Exeter</pre></p>
<p><pre>coffee</pre></p>

   <h2 class="saturn">Sample Output (Concise)</h2>

   <pre><code>
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "NA",
  "response": {
    "tokens": {
      "StreetName": "GATE REACH",
      "PostcodeOut": "EX2",
      "BuildingNumber": "7",
      "PostcodeIn": "6GA",
      "TownName": "EXETER",
      "Postcode": "EX2 6GA",
      "PaoStartNumber": "7"
    },
    "addresses": [
      {
        "uprn": "10023122457",
        "parentUprn": "0",
        "formattedAddress": "7 Gate Reach, Exeter, EX2 6GA",
        "formattedAddressNag": "7 Gate Reach, Exeter, EX2 6GA",
        "formattedAddressPaf": "7 Gate Reach, Exeter, EX2 6GA",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "7 Gate Reach, Exeter, EX2 6GA",
        "geo": {
          "latitude": 50.705948,
          "longitude": -3.5091076,
          "easting": 293535,
          "northing": 90677
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 98.0764,
        "underlyingScore": 27.280841827392578
      }
    ],
    "filter": "",
    "historical": true,
    "epoch": "",
    "rangekm": "",
    "latitude": "",
    "longitude": "",
    "limit": 10,
    "offset": 0,
    "total": 1,
    "sampleSize": 20,
    "maxScore": 27.280842,
    "matchthreshold": 5,
    "verbose": false,
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": []
}</code></pre>

   <h2 class="saturn">Sample Output (Verbose)</h2>
   <pre><code>
  {
    "apiVersion": "1.0.0-SNAPSHOT",
    "dataVersion": "NA",
    "response": {
      "tokens": {
        "StreetName": "GATE REACH",
        "PostcodeOut": "EX2",
        "BuildingNumber": "7",
        "PostcodeIn": "6GA",
        "TownName": "EXETER",
        "Postcode": "EX2 6GA",
        "PaoStartNumber": "7"
      },
      "addresses": [
        {
          "uprn": "10023122457",
          "parentUprn": "0",
          "relatives": [
            {
              "level": 1,
              "siblings": [
                10023122457
              ],
              "parents": []
            }
          ],
          "crossRefs": [
            {
              "crossReference": "osgb5000005114135395",
              "source": "7666MI"
            },
            {
              "crossReference": "osgb5000005112135238",
              "source": "7666MA"
            },
            {
              "crossReference": "E05003501",
              "source": "7666OW"
            },
            {
              "crossReference": "osgb5000005112092595",
              "source": "7666MT"
            },
            {
              "crossReference": "8933413000",
              "source": "7666VC"
            }
          ],
          "formattedAddress": "7 Gate Reach, Exeter, EX2 6GA",
          "formattedAddressNag": "7 Gate Reach, Exeter, EX2 6GA",
          "formattedAddressPaf": "7 Gate Reach, Exeter, EX2 6GA",
          "formattedAddressNisra": "",
          "welshFormattedAddressNag": "",
          "welshFormattedAddressPaf": "7 Gate Reach, Exeter, EX2 6GA",
          "paf": {
            "udprn": "52995192",
            "organisationName": "",
            "departmentName": "",
            "subBuildingName": "",
            "buildingName": "",
            "buildingNumber": "7",
            "dependentThoroughfare": "",
            "thoroughfare": "Gate Reach",
            "doubleDependentLocality": "",
            "dependentLocality": "",
            "postTown": "Exeter",
            "postcode": "EX2 6GA",
            "postcodeType": "S",
            "deliveryPointSuffix": "1H",
            "welshDependentThoroughfare": "",
            "welshThoroughfare": "Gate Reach",
            "welshDoubleDependentLocality": "",
            "welshDependentLocality": "",
            "welshPostTown": "Exeter",
            "poBoxNumber": "",
            "startDate": "2013-07-29T00:00:00+01:00",
            "endDate": ""
          },
          "nag": [
            {
              "uprn": "10023122457",
              "postcodeLocator": "EX2 6GA",
              "addressBasePostal": "D",
              "usrn": "14203041",
              "lpiKey": "1110L000168890",
              "pao": {
                "paoText": "",
                "paoStartNumber": "7",
                "paoStartSuffix": "",
                "paoEndNumber": "",
                "paoEndSuffix": ""
              },
              "sao": {
                "saoText": "",
                "saoStartNumber": "",
                "saoStartSuffix": "",
                "saoEndNumber": "",
                "saoEndSuffix": ""
              },
              "level": "",
              "officialFlag": "Y",
              "logicalStatus": "1",
              "streetDescriptor": "Gate Reach",
              "townName": "Exeter",
              "locality": "",
              "organisation": "",
              "legalName": "",
              "localCustodianCode": "1110",
              "localCustodianName": "Exeter",
              "localCustodianGeogCode": "E07000041",
              "lpiEndDate": "",
              "lpiStartDate": "2013-06-30T00:00:00+01:00"
            }
          ],
          "geo": {
            "latitude": 50.705948,
            "longitude": -3.5091076,
            "easting": 293535,
            "northing": 90677
          },
          "classificationCode": "RD",
          "census": {
            "addressType": "HH",
            "estabType": "Household",
            "countryCode": "E"
          },
          "lpiLogicalStatus": "1",
          "confidenceScore": 98.0764,
          "underlyingScore": 27.280841827392578
        }
      ],
      "filter": "",
      "historical": true,
      "epoch": "",
      "rangekm": "",
      "latitude": "",
      "longitude": "",
      "limit": 10,
      "offset": 0,
      "total": 1,
      "sampleSize": 20,
      "maxScore": 27.280842,
      "matchthreshold": 5,
      "verbose": true,
    },
    "status": {
      "code": 200,
      "message": "Ok"
    },
    "errors": []
  }
   </code></pre>
   