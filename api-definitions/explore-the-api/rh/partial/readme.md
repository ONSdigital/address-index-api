<h1 class="jupiter">/addresses/rh/partial</h1>



<p>Search by partial address (for type ahead). RH version</p>

<h2>Request</h2>

<p><code>GET /addresses/rh/partial</code></p>



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
            <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential, commercial, workplace or educational</td>
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
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "NA",
  "response": {
    "input": "corn ex",
    "addresses": [
      {
        "uprn": "10013049665",
        "bestMatchAddress": "Corn Exchange Office, 1 George Street, Exeter, EX1 1BU",
        "bestMatchAddressType": "PAF",
        "censusAddressType": "NA",
        "censusEstabType": "NA",
        "countryCode": "E"
      },
      {
        "uprn": "10023121714",
        "bestMatchAddress": "Cornerstone Housing LTD, Cornerstone House, Western Way, Exeter, EX1 1AL",
        "bestMatchAddressType": "PAF",
        "censusAddressType": "NA",
        "censusEstabType": "NA",
        "countryCode": "E"
      },
      {
        "uprn": "100041124274",
        "bestMatchAddress": "Exeter Corn Exchange, Market Street, Exeter, EX1 1BW",
        "bestMatchAddressType": "PAF",
        "censusAddressType": "NA",
        "censusEstabType": "NA",
        "countryCode": "E"
      },
      {
        "uprn": "10013038377",
        "bestMatchAddress": "The Real Cornish Pasty CO, 11 Martins Lane, Exeter, EX1 1EY",
        "bestMatchAddressType": "PAF",
        "censusAddressType": "NA",
        "censusEstabType": "NA",
        "countryCode": "E"
      },
      {
        "uprn": "100041124355",
        "bestMatchAddress": "Devon & Cornwall Constabulary, Heavitree Road, Exeter, EX1 2LR",
        "bestMatchAddressType": "PAF",
        "censusAddressType": "NA",
        "censusEstabType": "NA",
        "countryCode": "E"
      }
    ],
    "filter": "",
    "fallback": false,
    "epoch": "",
    "limit": 5,
    "offset": 0,
    "total": 161,
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
  "errors": [
    
  ]
}
</code></pre>