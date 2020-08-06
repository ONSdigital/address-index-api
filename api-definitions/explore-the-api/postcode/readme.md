<h1>/addresses/postcode/{postcode}</h1>

<p>Search for an address by postcode.</p>

<h2>Request</h2>

<p><code>GET /addresses/postcode/{postcode}</code></p>
   

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
        <td class="table--cell">offset</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Specifies the offset from zero, used for pagination.</td>
        <td class="table--cell">
            Optional
            <br>Default: 0
            <br>Maximum: 5000
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">limit</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Specifies the number of addresses to return.</td>
        <td class="table--cell">
            Optional
            <br>Default: 100
            <br>Maximum: 5000
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">classificationfilter</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential, commercial or workplace</td>
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
        <td class="table--cell">includeauxiliarysearch</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Search in the auxiliary index, if available</td>
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

   <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/postcode/{postcode}</code></pre>

<h2>Sample Queries</h2>

<p><pre>EX4 3ET</pre></p>
<p><pre>ex26ga</pre></p>
<p><pre>EX1 1LL</pre></p>

   <h2>Sample Output</h2>

   <pre><code>
 {
   "apiVersion": "1.0.0-SNAPSHOT",
   "dataVersion": "NA",
   "response": {
     "postcode": "EX43ET",
     "addresses": [
       {
         "uprn": "100040222182",
         "parentUprn": "0",
         "formattedAddress": "Body Language, 9 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "Body Language, 9 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "9 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "9 Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.72395,
           "longitude": -3.534838,
           "easting": 291760,
           "northing": 92717
         },
         "classificationCode": "CR08",
         "censusAddressType": "NA",
         "censusEstabType": "NA",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       },
       {
         "uprn": "10013038165",
         "parentUprn": "100040222182",
         "formattedAddress": "Flat 1, 9 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "Flat 1, 9 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "Flat 1, 9 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "Flat 1, 9 Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.72395,
           "longitude": -3.534838,
           "easting": 291760,
           "northing": 92717
         },
         "classificationCode": "RD06",
         "censusAddressType": "HH",
         "censusEstabType": "Household",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       },
       {
         "uprn": "100041045316",
         "parentUprn": "0",
         "formattedAddress": "10 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "10 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "10 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "10 Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.723988,
           "longitude": -3.5349243,
           "easting": 291754,
           "northing": 92721
         },
         "classificationCode": "C",
         "censusAddressType": "NA",
         "censusEstabType": "NA",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       },
       {
         "uprn": "100040222183",
         "parentUprn": "0",
         "formattedAddress": "11A Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "11A Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "11A Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "11A Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.723988,
           "longitude": -3.5349243,
           "easting": 291754,
           "northing": 92721
         },
         "classificationCode": "RD",
         "censusAddressType": "HH",
         "censusEstabType": "Household",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       },
       {
         "uprn": "10013047887",
         "parentUprn": "0",
         "formattedAddress": "11B Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "11B Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "11B Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "11B Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.723988,
           "longitude": -3.5349243,
           "easting": 291754,
           "northing": 92721
         },
         "classificationCode": "RD",
         "censusAddressType": "HH",
         "censusEstabType": "Household",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       },
       {
         "uprn": "100040222185",
         "parentUprn": "0",
         "formattedAddress": "12 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "12 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "12 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "12 Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.724037,
           "longitude": -3.5350108,
           "easting": 291748,
           "northing": 92727
         },
         "classificationCode": "RD",
         "censusAddressType": "HH",
         "censusEstabType": "Household",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       },
       {
         "uprn": "100041142136",
         "parentUprn": "100040222185",
         "formattedAddress": "The Flat, 12 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "The Flat, 12 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "Flat, 12 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "Flat, 12 Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.724037,
           "longitude": -3.5350108,
           "easting": 291748,
           "northing": 92727
         },
         "classificationCode": "RD06",
         "censusAddressType": "HH",
         "censusEstabType": "Household",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       },
       {
         "uprn": "10013043834",
         "parentUprn": "0",
         "formattedAddress": "Verve Hairdressing, 13 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "Verve Hairdressing, 13 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "13 Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "13 Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.724064,
           "longitude": -3.535112,
           "easting": 291740,
           "northing": 92729
         },
         "classificationCode": "CR08",
         "censusAddressType": "NA",
         "censusEstabType": "NA",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       },
       {
         "uprn": "100041142220",
         "parentUprn": "0",
         "formattedAddress": "The Maisonette, 13A Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "The Maisonette, 13A Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "13A Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "13A Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.72404,
           "longitude": -3.5350752,
           "easting": 291743,
           "northing": 92727
         },
         "classificationCode": "RD04",
         "censusAddressType": "HH",
         "censusEstabType": "Household",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       },
       {
         "uprn": "100041045317",
         "parentUprn": "0",
         "formattedAddress": "Premier Developments, 14A Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNag": "Premier Developments, 14A Lower North Street, Exeter, EX4 3ET",
         "formattedAddressPaf": "Kirkham Gould, 14A Lower North Street, Exeter, EX4 3ET",
         "formattedAddressNisra": "",
         "welshFormattedAddressNag": "",
         "welshFormattedAddressPaf": "Kirkham Gould, 14A Lower North Street, Exeter, EX4 3ET",
         "geo": {
           "latitude": 50.7241,
           "longitude": -3.535154,
           "easting": 291738,
           "northing": 92733
         },
         "classificationCode": "CI",
         "censusAddressType": "NA",
         "censusEstabType": "NA",
         "countryCode": "E",
         "lpiLogicalStatus": "1",
         "confidenceScore": 100,
         "underlyingScore": 0
       }
     ],
     "filter": "",
     "historical": true,
     "epoch": "",
     "limit": 10,
     "offset": 0,
     "total": 74,
     "maxScore": 0,
     "verbose": false
   },
   "status": {
     "code": 200,
     "message": "Ok"
   },
   "errors": []
 }
</code></pre>