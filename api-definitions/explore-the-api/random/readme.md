<h1>/addresses/random</h1>

<p>Search for a random address.</p>

<h2>Request</h2>

<p><code>GET /addresses/random</code></p>

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
        <td class="table--cell">classificationfilter</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential or commercial</td>
        <td class="table--cell">Optional</td>
    </tr>    
    <tr class="table--row">
        <td class="table--cell">limit</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Specifies the number of addresses to return.</td>
        <td class="table--cell">
            Optional
            <br>Default: 1
            <br>Maximum: 100
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
          <td class="table--cell">fromsource</td>
          <td class="table--cell">string</td>
          <td class="table--cell">Set to nionly or ewonly to filter Northern Ireland addresses (Census index only)</td>
          <td class="table--cell">
              Optional
              <br>Default: all
          </td>
     /tr>
  </tbody>
</table>

<h2>Responses</h2>

<h3>400</h3>
<p>Bad request. Indicates an issue with the request. Further details are provided in the response.</p>

<h3>401</h3>
<p>Unauthorised. The API key provided with the request is invalid.</p>

<h3>500</h3>
<p>Internal server error. Failed to process the request due to an internal error.</p>

<h3>200</h3>
<p>Success. A json return of matched addresses.</p>

<h3>429</h3>
<p>Server too busy. The Address Index API is experiencing exceptional load.</p>

<h2>CURL example</h2>

<div class="markdown">
    <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/random</code></pre>
</div>

   <h2>Sample Output</h2>

   <pre><code>
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "NA",
  "response": {
    "addresses": [
      {
        "uprn": "10013048713",
        "parentUprn": "100040229521",
        "formattedAddress": "Garden Flat, 17 Powderham Crescent, Exeter, EX4 6DA",
        "formattedAddressNag": "Garden Flat, 17 Powderham Crescent, Exeter, EX4 6DA",
        "formattedAddressPaf": "Garden Flat, Rear Of 17, Powderham Crescent, Exeter, EX4 6DA",
        "formattedAddressNisra": "",
        "welshFormattedAddressNag": "",
        "welshFormattedAddressPaf": "Garden Flat, Rear Of 17, Powderham Crescent, Exeter, EX4 6DA",
        "geo": {
          "latitude": 50.731503,
          "longitude": -3.5233943,
          "easting": 292585,
          "northing": 93540
        },
        "classificationCode": "RD06",
        "censusAddressType": "HH",
        "censusEstabType": "Household",
        "countryCode": "E",
        "lpiLogicalStatus": "1",
        "confidenceScore": 100,
        "underlyingScore": 0.999169111251831
      }
    ],
    "filter": "RD0*",
    "historical": true,
    "epoch": "",
    "limit": 1,
    "verbose": false,
    "fromsource": "all"
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": []
}
</code></pre>