<h1 class="jupiter">/addresses/eq</h1>

<p>Search by partial address or postcode (custom output for EQ).</p>

<h3>Subordinate Endpoints</h3>

<table class="table">
    <thead class="table--head">
    <th scope="col" class="table--header--cell">Method</th>
    <th scope="col" class="table--header--cell">Endpoint</th>
    <th scope="col" class="table--header--cell">Description</th>
    </thead>
    <tbody>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="uprn/readme.md">addresses/eq/uprn/{uprn}</a></td>
        <td class="table--cell">
            Fetch single address by uprn with chosen address type.
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="bucket/readme.md">addresses/eq/bucket</a></td>
        <td class="table--cell">
            Fetch addresses matching postcode / street / town combination.
        </td>
    </tr>
    </tbody>
</table>

<h2>Request</h2>

<p><code>GET /addresses/eq</code></p>

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
                <br>Maximum: 250 partials or 5000 postcodes
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">limit</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the number of addresses to return.</td>
            <td class="table--cell">
                Optional
                <br>Default: 20 partials or 100 postcodes
                <br>Maximum: 100 partials or 5000 postcodes
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">classificationfilter</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential, commercial, educational or workplace</td>
            <td class="table--cell">
                Optional
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
            <tr class="table--row">
                <td class="table--cell">lboost</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Weighting for addresses in Channel Islands as a decimal from 0 to 10</td>
                <td class="table--cell">
                     Optional
                    <br>Default: 1.0
                </td>
            </tr> 
            <tr class="table--row">
                <td class="table--cell">mboost</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Weighting for addresses in Isle of Man as a decimal from 0 to 10</td>
                <td class="table--cell">
                     Optional
                    <br>Default: 1.0
                </td>
            </tr> 
            <tr class="table--row">
                <td class="table--cell">jboost</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Weighting for addresses not allocated to a country as a decimal from 0 to 10</td>
                <td class="table--cell">
                     Optional
                    <br>Default: 1.0
                </td>
            </tr> 
        <tr class="table--row">
            <td class="table--cell">groupfullpostcodes</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Group full postcode output, yes, no or combo (combined response, slower)</td>
            <td class="table--cell">
                Optional
                <br>Default: no
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

<p><pre>corn exc</pre></p>
<p><pre>3 EX26GA</pre></p>
<p><pre>EX4 3</pre></p>

   <h2>Sample Output</h2>
   
   <pre><code>
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "NA",
  "termsAndConditions": "https://census.gov.uk/terms-and-conditions",
  "response": {
    "input": "Corn Exc",
    "addresses": [
      {
        "uprn": "10013049665",
        "bestMatchAddress": "Corn Exchange Office, 1 George Street, Exeter, EX1 1BU",
        "bestMatchAddressType": "PAF"
      },
      {
        "uprn": "100041124274",
        "bestMatchAddress": "Exeter Corn Exchange, Market Street, Exeter, EX1 1BW",
        "bestMatchAddressType": "PAF"
      }
    ],
    "filter": "",
    "fallback": false,
    "historical": false,
    "epoch": "",
    "limit": 20,
    "offset": 0,
    "total": 2,
    "maxScore": 0,
    "highlight": "on",
    "favourpaf": true,
    "favourwelsh": false,
    "eboost": 1,
    "nboost": 1,
    "sboost": 1,
    "wboost": 1
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": [
    
  ]
}
</code></pre>
   <pre><code>
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "NA",
  "termsAndConditions": "https://census.gov.uk/terms-and-conditions",
  "response": {
    "input": "3 EX26GA",
    "addresses": [
      {
        "uprn": "10023122453",
        "bestMatchAddress": "3 Gate Reach, Exeter, EX2 6GA",
        "bestMatchAddressType": "PAF"
      },
      {
        "uprn": "10023122474",
        "bestMatchAddress": "30 Gate Reach, Exeter, EX2 6GA",
        "bestMatchAddressType": "PAF"
      },
      {
        "uprn": "10023122475",
        "bestMatchAddress": "32 Gate Reach, Exeter, EX2 6GA",
        "bestMatchAddressType": "PAF"
      },
      {
        "uprn": "10023122476",
        "bestMatchAddress": "34 Gate Reach, Exeter, EX2 6GA",
        "bestMatchAddressType": "PAF"
      }
    ],
    "filter": "",
    "fallback": false,
    "epoch": "",
    "limit": 20,
    "offset": 0,
    "total": 4,
    "maxScore": 0,
    "favourpaf": true,
    "favourwelsh": false,
    "eboost": 1,
    "nboost": 1,
    "sboost": 1,
    "wboost": 1
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": []
}
</code></pre>

   <pre><code>
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "NA",
  "termsAndConditions": "https://census.gov.uk/terms-and-conditions",
  "response": {
    "partpostcode": "EX4 3",
    "postcodes": [
      {
        "postcode": "EX4 3AA",
        "streetName": "Bonhay Road",
        "townName": "Exeter",
        "addressCount": 11,
        "firstUprn": 100040203926
      },
      {
        "postcode": "EX4 3AB",
        "streetName": "Bonhay Road",
        "townName": "Exeter",
        "addressCount": 5,
        "firstUprn": 10013046878
      },
      {
        "postcode": "EX4 3AD",
        "streetName": "Bartholomew Street West",
        "townName": "Exeter",
        "addressCount": 75,
        "firstUprn": 10013047777
      },
      {
        "postcode": "EX4 3AE",
        "streetName": "Bartholomew Street West",
        "townName": "Exeter",
        "addressCount": 50,
        "firstUprn": 10013040310
      },
      {
        "postcode": "EX4 3AG",
        "streetName": "Bartholomew Street West",
        "townName": "Exeter",
        "addressCount": 7,
        "firstUprn": 10013044374
      },
      {
        "postcode": "EX4 3AH",
        "streetName": "New Bridge Street",
        "townName": "Exeter",
        "addressCount": 53,
        "firstUprn": 10013038581
      }
     ],
    "filter": "",
    "epoch": "",
    "limit": 6,
    "offset": 0,
    "total": 143,
    "maxScore": 1,
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": [
    
  ]
}
</code></pre>