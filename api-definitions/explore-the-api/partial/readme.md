<h1 class="jupiter">/addresses/partial</h1>



<p>Search by partial address (for type ahead).</p>

<h2>Request</h2>

<p><code>GET /addresses/partial</code></p>



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
            <td class="table--cell">Specifies the address input.</td>
            <td class="table--cell">
                Required
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">fallback</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies whether a slow fallback query is used in the event of the main query returning no results.</td>
            <td class="table--cell">
                Optional
                <br>Default: False
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
            <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential, educational, commercial or workplace</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
        <tr>
            <td class="table--cell">historical</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Include historical addresses</td>
            <td class="table--cell">
                Optional
                <br>Default: False
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
            <td class="table--cell">highlight</td>
            <td class="table--cell">string</td>
            <td class="table--cell">include detailed highlighting info, options are off, on or debug</td>
            <td class="table--cell">
                Optional
                <br>Default: on
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">favourpaf</td>
            <td class="table--cell">string</td>
            <td class="table--cell">paf beats nag on draw for best match</td>
            <td class="table--cell">
                Optional
                <br>Default: True
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">favourwelsh</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Welsh beats English on draw for best match</td>
            <td class="table--cell">
                Optional
                <br>Default: False
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">includeauxiliarysearch</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Search in the auxiliary index, if available</td>
            <td class="table--cell">
                Optional
                <br>Default: false
            </td>
        </tr>
        <tr class="table--row">
           <td class="table--cell">eboost</td>
           <td class="table--cell">string</td>
           <td class="table--cell">Weighting for addresses in England as a decimal from 0 to 10</td>
           <td class="table--cell">
                Optional
                <br>Default: 1.0
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">nboost</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Weighting for addresses in Northern Ireland as a decimal from 0 to 10</td>
            <td class="table--cell">
                Optional
                <br>Default: 1.0
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">sboost</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Weighting for addresses in Scotland as a decimal from 0 to 10</td>
            <td class="table--cell">
                Optional
                <br>Default: 1.0
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">wboost</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Weighting for addresses in Wales as a decimal from 0 to 10</td>
            <td class="table--cell">
                 Optional
                <br>Default: 1.0
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

   <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/partial</code></pre>

<h2>Sample Queries</h2>

<p><pre>corn ex</pre></p>
<p><pre>4 EX26GA</pre></p>

   <h2>Sample Output</h2>



   <pre><code>
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "NA",
  "response": {
    "input": "CORN EX",
    "addresses": [
      {
        "uprn": "10013049665",
        "parentUprn": "0",
        "formattedAddress": "Exeter Corn Exchange Office, 1 George Street, Exeter, EX1 1BU",
        "formattedAddressNag": "Exeter Corn Exchange Office, 1 George Street, Exeter, EX1 1BU",
        "formattedAddressPaf": "Corn Exchange Office, 1 George Street, Exeter, EX1 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "Corn Exchange Office, 1 George Street, Exeter, EX1 1BU",
        "highlights": {
          "bestMatchAddress": "Corn Exchange Office, 1 George Street, Exeter, EX1 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.721687,
          "longitude": -3.5324543,
          "easting": 291923,
          "northing": 92461
        },
        "classificationCode": "CO01",
        "census": {
          "addressType": "NA",
          "estabType": "NA",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 25,
        "underlyingScore": 5
      },
      {
        "uprn": "10023121714",
        "parentUprn": "0",
        "formattedAddress": "Cornerstone House, Western Way, St Davids, Exeter, EX1 1AL",
        "formattedAddressNag": "Cornerstone House, Western Way, St Davids, Exeter, EX1 1AL",
        "formattedAddressPaf": "Cornerstone Housing LTD, Cornerstone House, Western Way, Exeter, EX1 1AL",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "Cornerstone Housing LTD, Cornerstone House, Western Way, Exeter, EX1 1AL",
        "highlights": {
          "bestMatchAddress": "Cornerstone Housing LTD, Cornerstone House, Western Way, Exeter, EX1 1AL",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.72007,
          "longitude": -3.5316205,
          "easting": 291978,
          "northing": 92280
        },
        "classificationCode": "CO01",
        "censusAddressType": "NA",
        "censusEstabType": "NA",
        "countryCode": "E",
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100041124274",
        "parentUprn": "0",
        "formattedAddress": "Exeter Corn Exchange, Market Street, Exeter, EX1 1BW",
        "formattedAddressNag": "Exeter Corn Exchange, Market Street, Exeter, EX1 1BW",
        "formattedAddressPaf": "Exeter Corn Exchange, Market Street, Exeter, EX1 1BW",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "Exeter Corn Exchange, Market Street, Exeter, EX1 1BW",
        "highlights": {
          "bestMatchAddress": "Exeter Corn Exchange, Market Street, Exeter, EX1 1BW",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.72167,
          "longitude": -3.5327232,
          "easting": 291904,
          "northing": 92460
        },
        "classificationCode": "CO01",
        "censusAddressType": "NA",
        "censusEstabType": "NA",
        "countryCode": "E",
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "10013038377",
        "parentUprn": "0",
        "formattedAddress": "The Real Cornish Pasty, 11 Martins Lane, Exeter, EX1 1EY",
        "formattedAddressNag": "The Real Cornish Pasty, 11 Martins Lane, Exeter, EX1 1EY",
        "formattedAddressPaf": "The Real Cornish Pasty CO, 11 Martins Lane, Exeter, EX1 1EY",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "The Real Cornish Pasty CO, 11 Martins Lane, Exeter, EX1 1EY",
        "highlights": {
          "bestMatchAddress": "The Real Cornish Pasty CO, 11 Martins Lane, Exeter, EX1 1EY",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.72355,
          "longitude": -3.530532,
          "easting": 292063,
          "northing": 92666
        },
        "classificationCode": "CR08",
        "censusAddressType": "NA",
        "censusEstabType": "NA",
        "countryCode": "E",
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100041124355",
        "parentUprn": "0",
        "formattedAddress": "Devon And Cornwall Constabulary, Heavitree Road, Exeter, EX1 2LR",
        "formattedAddressNag": "Devon And Cornwall Constabulary, Heavitree Road, Exeter, EX1 2LR",
        "formattedAddressPaf": "Devon & Cornwall Constabulary, Heavitree Road, Exeter, EX1 2LR",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "Devon & Cornwall Constabulary, Heavitree Road, Exeter, EX1 2LR",
        "highlights": {
          "bestMatchAddress": "Devon & Cornwall Constabulary, Heavitree Road, Exeter, EX1 2LR",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.72412,
          "longitude": -3.517176,
          "easting": 293007,
          "northing": 92710
        },
        "classificationCode": "CX01PT",
        "census": {
          "addressType": "NA",
          "estabType": "NA",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "10023121744",
        "parentUprn": "0",
        "formattedAddress": "Cornerstone, 12A Wykes Road, Exeter, EX1 2UG",
        "formattedAddressNag": "Cornerstone, 12A Wykes Road, Exeter, EX1 2UG",
        "formattedAddressPaf": "Cornerstone Housing LTD, 12A Wykes Road, Exeter, EX1 2UG",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "Cornerstone Housing LTD, 12A Wykes Road, Exeter, EX1 2UG",
        "highlights": {
          "bestMatchAddress": "Cornerstone Housing LTD, 12A Wykes Road, Exeter, EX1 2UG",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.72921,
          "longitude": -3.5074413,
          "easting": 293705,
          "northing": 93261
        },
        "classificationCode": "CR08",
        "census": {
          "addressType": "NA",
          "estabType": "NA",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "10013041300",
        "parentUprn": "10023119580",
        "formattedAddress": "Devon And Cornwall Constabulary Police Federation, Unit 2, River Court, EX2 5JL",
        "formattedAddressNag": "Devon And Cornwall Constabulary Police Federation, Unit 2, River Court, EX2 5JL",
        "formattedAddressPaf": "Police Federation, Devon & Cornwall Constabulary, Unit 2, River Court, Pynes Hill, Exeter, EX2 5JL",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "Police Federation, Devon & Cornwall Constabulary, Unit 2, River Court, Pynes Hill, Exeter, EX2 5JL",
        "highlights": {
          "bestMatchAddress": "Police Federation, Devon & Cornwall Constabulary, Unit 2, River Court, Pynes Hill, Exeter, EX2 5JL",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.708397,
          "longitude": -3.4901402,
          "easting": 294880,
          "northing": 90923
        },
        "classificationCode": "CX01PT",
        "census": {
          "addressType": "NA",
          "estabType": "NA",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "10013041308",
        "parentUprn": "10013050589",
        "formattedAddress": "Devon And Cornwall Police Authority, Endeavour House, EX2 5WH",
        "formattedAddressNag": "Devon And Cornwall Police Authority, Endeavour House, EX2 5WH",
        "formattedAddressPaf": "",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "",
        "highlights": {
          "bestMatchAddress": "Devon And Cornwall Police Authority, Endeavour House, EX2 5WH",
          "source": "L",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.706276,
          "longitude": -3.489223,
          "easting": 294940,
          "northing": 90686
        },
        "classificationCode": "C",
        "census": {
          "addressType": "NA",
          "estabType": "NA",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "10013040774",
        "parentUprn": "100041225283",
        "formattedAddress": "Devon And Cornwall Police, Gilbert House, Grace Road West, Exeter, EX2 8PU",
        "formattedAddressNag": "Devon And Cornwall Police, Gilbert House, Grace Road West, Exeter, EX2 8PU",
        "formattedAddressPaf": "Scribblez Day Nursery, Gilbert House, Grace Road West, Marsh Barton Trading Estate, Exeter, EX2 8PU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "Scribblez Day Nursery, Gilbert House, Grace Road West, Marsh Barton Trading Estate, Exeter, EX2 8PU",
        "highlights": {
          "bestMatchAddress": "Devon And Cornwall Police, Gilbert House, Grace Road West, Exeter, EX2 8PU",
          "source": "L",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.705193,
          "longitude": -3.531612,
          "easting": 291944,
          "northing": 90626
        },
        "classificationCode": "CE02",
        "census": {
          "addressType": "NA",
          "estabType": "NA",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209174",
        "parentUprn": "0",
        "formattedAddress": "1 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "1 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "1 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "1 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "1 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.719097,
          "longitude": -3.5441144,
          "easting": 291094,
          "northing": 92191
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209175",
        "parentUprn": "0",
        "formattedAddress": "2 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "2 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "2 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "2 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "2 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.718845,
          "longitude": -3.5441911,
          "easting": 291088,
          "northing": 92163
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209176",
        "parentUprn": "0",
        "formattedAddress": "3 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "3 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "3 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "3 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "3 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.719105,
          "longitude": -3.5441713,
          "easting": 291090,
          "northing": 92192
        },
        "classificationCode": "RD",
        "censusAddressType": "HH",
        "censusEstabType": "Household",
        "countryCode": "E",
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209177",
        "parentUprn": "0",
        "formattedAddress": "4 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "4 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "4 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "4 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "4 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.718853,
          "longitude": -3.544248,
          "easting": 291084,
          "northing": 92164
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209178",
        "parentUprn": "0",
        "formattedAddress": "5 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "5 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "5 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "5 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "5 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.719124,
          "longitude": -3.5442426,
          "easting": 291085,
          "northing": 92194
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209179",
        "parentUprn": "0",
        "formattedAddress": "6 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "6 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "6 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "6 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "6 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.71886,
          "longitude": -3.544305,
          "easting": 291080,
          "northing": 92165
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209180",
        "parentUprn": "0",
        "formattedAddress": "7 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "7 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "7 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "7 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "7 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.719097,
          "longitude": -3.5442984,
          "easting": 291081,
          "northing": 92191
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209181",
        "parentUprn": "0",
        "formattedAddress": "8 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "8 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "8 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "8 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "8 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.71888,
          "longitude": -3.5443764,
          "easting": 291075,
          "northing": 92167
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209182",
        "parentUprn": "0",
        "formattedAddress": "9 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "9 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "9 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "9 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "9 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.719093,
          "longitude": -3.5443552,
          "easting": 291077,
          "northing": 92191
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209183",
        "parentUprn": "0",
        "formattedAddress": "10 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "10 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "10 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "10 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "10 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.71888,
          "longitude": -3.5444188,
          "easting": 291072,
          "northing": 92167
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      },
      {
        "uprn": "100040209184",
        "parentUprn": "0",
        "formattedAddress": "11 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNag": "11 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressPaf": "11 Cornwall Street, Exeter, EX4 1BU",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "11 Cornwall Street, Exeter, EX4 1BU",
        "highlights": {
          "bestMatchAddress": "11 Cornwall Street, Exeter, EX4 1BU",
          "source": "P",
          "lang": "E"
        },
        "geo": {
          "latitude": 50.719105,
          "longitude": -3.5444262,
          "easting": 291072,
          "northing": 92192
        },
        "classificationCode": "RD",
        "census": {
          "addressType": "HH",
          "estabType": "Household",
          "countryCode": "E"
        },
        "lpiLogicalStatus": "1",
        "confidenceScore": 15,
        "underlyingScore": 3
      }
    ],
    "filter": "",
    "fallback": true,
    "historical": false,
    "epoch": "",
    "limit": 20,
    "offset": 0,
    "total": 161,
    "maxScore": 0,
    "verbose": false,
    "highlight": "on",
    "favourpaf": true,
    "favourwelsh": false
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": []
}
</code></pre>