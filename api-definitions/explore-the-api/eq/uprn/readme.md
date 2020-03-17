<h1 class="jupiter">/addresses/eq/uprn/{uprn}</h1>

<p>Fetch single address by UPRN with named address type (custom output for EQ).</p>

<h2>Request</h2>

<p><code>GET /addresses/eq/uprn/{uprn}</code></p>



<h3>Query parameters</h3>

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
            <td class="table--cell">addresstype</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Version of the address to return - paf, welshpaf, nag, welshnag, nisra</td>
            <td class="table--cell">
               Required
               <br>Default: None Set                
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
    

   <h2>CURL example</h2>

   <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/eq/uprn</code></pre>

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