<h1>/addresses/groupedpostcode/{postcode}</h1>

<p>Search for postcodes matching a partail postcode string.</p>

<h2>Request</h2>

<p><code>GET /addresses/groupedpostcode/{postcode}</code></p>
   

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
        <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential, commercial, workplace or educational</td>
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

<p><pre>EX4</pre></p>
<p><pre>EX4 3</pre></p>
<p><pre>EX4 3A</pre></p>

   <h2>Sample Output</h2>

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
      },
      {
        "postcode": "EX4 3AJ",
        "streetName": "Bartholomew Street West",
        "townName": "Exeter",
        "addressCount": 44,
        "firstUprn": 10013036107
      },
      {
        "postcode": "EX4 3AL",
        "streetName": "Bartholomew Street West",
        "townName": "Exeter",
        "addressCount": 49,
        "firstUprn": 100040202198
      },
      {
        "postcode": "EX4 3AN",
        "streetName": "Fore Street",
        "townName": "Exeter",
        "addressCount": 106,
        "firstUprn": 10013036993
      },
      {
        "postcode": "EX4 3AP",
        "streetName": "Friernhay Street",
        "townName": "Exeter",
        "addressCount": 8,
        "firstUprn": 100040214144
      },
      {
        "postcode": "EX4 3AQ",
        "streetName": "New Bridge Street",
        "townName": "Exeter",
        "addressCount": 6,
        "firstUprn": 10091470006
      },
      {
        "postcode": "EX4 3AR",
        "streetName": "Friernhay Street",
        "townName": "Exeter",
        "addressCount": 24,
        "firstUprn": 10013046959
      },
      {
        "postcode": "EX4 3AT",
        "streetName": "Fore Street",
        "townName": "Exeter",
        "addressCount": 34,
        "firstUprn": 10013042151
      },
      {
        "postcode": "EX4 3AT",
        "streetName": "St Olaves Close",
        "townName": "Exeter",
        "addressCount": 1,
        "firstUprn": 100041142118
      }
    ],
    "filter": "",
    "historical": false,
    "epoch": "",
    "limit": 14,
    "offset": 0,
    "total": 143,
    "maxScore": 1,
    "verbose": false
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": [
    
  ]
}
</code></pre>