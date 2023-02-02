<h1>/addresses/uprn/{uprn}</h1>

<p>Gets an address by UPRN.</p>

<h2>Request</h2>

<p><code>GET /addresses/uprn/{uprn}</code></p>

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
            <td class="table--cell">includeauxiliarysearch</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Search in the auxiliary index, if available</td>
            <td class="table--cell">
                Optional
                <br>Default: false
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
    
<h3>404</h3>
<p>Not found. The requested UPRN was not found in the index.</p>
    
<h3>400</h3>
<p>Bad request. Indicates an issue with the request. Further details are provided in the response.</p>
    
<h3>401</h3>
<p>Unauthorised. The API key provided with the request is invalid.</p>
    
<h3>500</h3>
<p>Internal server error. Failed to process the request due to an internal error.</p>
    
<h3>200</h3>
<p>Success. A json return of matched addresses.</p>
    
<h3 class="neptune">429</h3>
<p>Server too busy. The Address Index API is experiencing exceptional load.</p>
    
<h2>CURL example</h2>

<pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/uprn/{uprn}</code></pre>

<h2>Sample Queries</h2>

<p><pre>10013049457</pre></p>
<p><pre>10023123829</pre></p>
<p><pre>10023122457</pre></p>

<h2>Sample Output</h2>

<pre><code>
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "NA",
  "response": {
    "address": {
      "uprn": "10013049457",
      "parentUprn": "0",
      "formattedAddress": "Costa Coffee, 12 Bedford Street, Exeter, EX1 1LL",
      "formattedAddressNag": "Costa Coffee, 12 Bedford Street, Exeter, EX1 1LL",
      "formattedAddressPaf": "Costa Coffee, 12 Bedford Street, Exeter, EX1 1LL",
      "formattedAddressNisra": "",
      "welshFormattedAddressNag": "",
      "welshFormattedAddressPaf": "Costa Coffee, 12 Bedford Street, Exeter, EX1 1LL",
      "geo": {
        "latitude": 50.723785,
        "longitude": -3.5288017,
        "easting": 292185,
        "northing": 92689
      },
      "classificationCode": "CR07",
      "census": {
        "addressType": "HH",
        "estabType": "Household",
        "countryCode": "E"
      },
      "lpiLogicalStatus": "1",
      "confidenceScore": 100,
      "underlyingScore": 1
    },
    "historical": true,
    "epoch": "",
    "verbose": false
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": []
}
</code></pre>
