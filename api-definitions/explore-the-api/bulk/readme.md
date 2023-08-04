<h1>/bulk</h1>

<p>Runs a batch of up to 30000 addresses. Use bulk-full for extra info</p>

<h2>Request</h2>

<p><code>POST /bulk</code></p>

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
            <td class="table--cell">limitperaddress</td>
            <td class="table--cell">integer</td>
            <td class="table--cell">Specifies the maximum number of matches to return per address.</td>
            <td class="table--cell">
                Optional
                <br>Default: 1
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">historical</td>
            <td class="table--cell">boolean</td>
            <td class="table--cell">Include historical addresses</td>
            <td class="table--cell">
                Optional
                <br>Default: True
             </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">matchthreshold</td>
            <td class="table--cell">number</td>
            <td class="table--cell">Minimum confidence score (percentage) for match to be included in results.</td>
            <td class="table--cell">
                Optional
                <br>Default: 10
             </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">epoch</td>
            <td class="table--cell">string</td>
            <td class="table--cell"></td>
            <td class="table--cell">
                Optional
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

<pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/bulk</code></pre>

<h2>Sample Queries</h2>

<p><pre>{
    "addresses":[{
        "id" : "1",
        "address": "45 Wheatlands Stevenage SG2 0JT"
    },{
        "id" : "2",
        "address": "86 Truro Drive Kidderminster DY11 6DL"
    },{
        "id" : "3",
        "address": "61 Elmhurst Mansions Edgeley Road London SW4 6EU"
    }]
}</pre></p>

<h2>Sample Output</h2>

<pre><code>
{
    "apiVersion": "1.0.4",
    "dataVersion": "89",
    "bulkAddresses": [
        {
            "id": "1",
            "inputAddress": "45 Wheatlands Stevenage SG2 0JT",
            "uprn": "100080900305",
            "parentuprn": "0",
            "udprn": "",
            "matchedFormattedAddress": "45 Wheatlands, Stevenage, SG2 0JT",
            "tokens": {
                "StreetName": "WHEATLANDS",
                "PostcodeIn": "0JT",
                "TownName": "STEVENAGE",
                "Postcode": "SG2 0JT",
                "PaoStartNumber": "45",
                "PostcodeOut": "SG2",
                "BuildingNumber": "45"
            },
            "confidenceScore": 97.1951,
            "underlyingScore": 38.15106964111328
        },
        {
            "id": "2",
            "inputAddress": "86 Truro Drive Kidderminster DY11 6DL",
            "uprn": "100120740838",
            "parentuprn": "0",
            "udprn": "",
            "matchedFormattedAddress": "86 Truro Drive, Kidderminster, DY11 6DL",
            "tokens": {
                "StreetName": "TRURO DRIVE",
                "PostcodeIn": "6DL",
                "TownName": "KIDDERMINSTER",
                "Postcode": "DY11 6DL",
                "PaoStartNumber": "86",
                "PostcodeOut": "DY11",
                "BuildingNumber": "86"
            },
            "confidenceScore": 96.9148,
            "underlyingScore": 41.054481506347656
        },
        {
            "id": "3",
            "inputAddress": "61 Elmhurst Mansions Edgeley Road London SW4 6EU",
            "uprn": "100021835420",
            "parentuprn": "0",
            "udprn": "",
            "matchedFormattedAddress": "61, Elmhurst Mansions, Edgeley Road, London, SW4 6EU",
            "tokens": {
                "StreetName": "EDGELEY ROAD",
                "PostcodeOut": "SW4",
                "PostcodeIn": "6EU",
                "TownName": "LONDON",
                "Postcode": "SW4 6EU",
                "PaoStartNumber": "61",
                "BuildingName": "61 ELMHURST MANSIONS"
            },
            "confidenceScore": 82.6124,
            "underlyingScore": 46.813934326171875
        },
        {
            "id": "3",
            "inputAddress": "61 Elmhurst Mansions Edgeley Road London SW4 6EU",
            "uprn": "200000462484",
            "parentuprn": "0",
            "udprn": "",
            "matchedFormattedAddress": "Flats 61 To 66, Elmhurst Mansions, Edgeley Road, London, SW4 6EU",
            "tokens": {
                "StreetName": "EDGELEY ROAD",
                "PostcodeOut": "SW4",
                "PostcodeIn": "6EU",
                "TownName": "LONDON",
                "Postcode": "SW4 6EU",
                "PaoStartNumber": "61",
                "BuildingName": "61 ELMHURST MANSIONS"
            },
            "confidenceScore": 52.8627,
            "underlyingScore": 41.701568603515625
        },
        {
            "id": "3",
            "inputAddress": "61 Elmhurst Mansions Edgeley Road London SW4 6EU",
            "uprn": "10023852933",
            "parentuprn": "0",
            "udprn": "",
            "matchedFormattedAddress": "84A, Elmhurst Mansions, Edgeley Road, London, SW4 6EU",
            "tokens": {
                "StreetName": "EDGELEY ROAD",
                "PostcodeOut": "SW4",
                "PostcodeIn": "6EU",
                "TownName": "LONDON",
                "Postcode": "SW4 6EU",
                "PaoStartNumber": "61",
                "BuildingName": "61 ELMHURST MANSIONS"
            },
            "confidenceScore": 52.7931,
            "underlyingScore": 40.90314483642578
        },
        {
            "id": "3",
            "inputAddress": "61 Elmhurst Mansions Edgeley Road London SW4 6EU",
            "uprn": "100021835414",
            "parentuprn": "0",
            "udprn": "",
            "matchedFormattedAddress": "55, Elmhurst Mansions, Edgeley Road, London, SW4 6EU",
            "tokens": {
                "StreetName": "EDGELEY ROAD",
                "PostcodeOut": "SW4",
                "PostcodeIn": "6EU",
                "TownName": "LONDON",
                "Postcode": "SW4 6EU",
                "PaoStartNumber": "61",
                "BuildingName": "61 ELMHURST MANSIONS"
            },
            "confidenceScore": 52.7931,
            "underlyingScore": 40.90314483642578
        },
        {
            "id": "3",
            "inputAddress": "61 Elmhurst Mansions Edgeley Road London SW4 6EU",
            "uprn": "100021835415",
            "parentuprn": "0",
            "udprn": "",
            "matchedFormattedAddress": "56, Elmhurst Mansions, Edgeley Road, London, SW4 6EU",
            "tokens": {
                "StreetName": "EDGELEY ROAD",
                "PostcodeOut": "SW4",
                "PostcodeIn": "6EU",
                "TownName": "LONDON",
                "Postcode": "SW4 6EU",
                "PaoStartNumber": "61",
                "BuildingName": "61 ELMHURST MANSIONS"
            },
            "confidenceScore": 52.7931,
            "underlyingScore": 40.90314483642578
        }
    ],
    "status": {
        "code": 200,
        "message": "Ok"
    },
    "errors": []
}
</code></pre>
