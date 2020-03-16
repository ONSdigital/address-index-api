<h1 class="jupiter">/addresses/partial</h1>



<p>Search by partial address or postcode (custom output for EQ).</p>

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
            <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential or commercial</td>
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
            <td class="table--cell">Set to niboost to favour Northern Ireland results, nionly or ewonly to filter (Census index only)</td>
            <td class="table--cell">
                Optional
                <br>Default: all
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
    "fallback": true,
    "historical": false,
    "epoch": "",
    "limit": 20,
    "offset": 0,
    "total": 4,
    "maxScore": 0,
    "verbose": false,
    "fromsource": "all",
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