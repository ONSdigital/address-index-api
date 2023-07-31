<h1>/addresses/ids</h1>

<p>Search for an address. Custom version for IDS (addressEntryId used to link to other datasets)</p>

<h2>Request</h2>

<p><code>GET /addresses/ids</code></p>
 

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
            <td class="table--cell">Specifies the address search string (e.g. &#39;14 Acacia Avenue, Ruislip, HA4 8RG&#39;).</td>
            <td class="table--cell">
                Required
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
            <td class="table--cell">rangekm</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Limit results to those within this number of kilometers of point (decimal e.g. 0.1)</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
          <tr class="table--row">
            <td class="table--cell">lat</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Latitude of point in decimal format (e.g. 50.705948).</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">lon</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Longitude of point in decimal format (e.g. -3.5091076).</td>
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
                <br>Default: True
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">matchthreshold</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Minimum confidence score (percentage) for match to be included in results.</td>
            <td class="table--cell">
                Optional
                <br>Default: 5
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
            <tr class="table--row">
                <td class="table--cell">lboost</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Set to 0 to exclude addresses in Channel Islands</td>
                <td class="table--cell">
                     Optional
                    <br>Default: 1.0
                </td>
            </tr> 
            <tr class="table--row">
                <td class="table--cell">mboost</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Set to 0 to exclude addresses in Isle of Man</td>
                <td class="table--cell">
                     Optional
                    <br>Default: 1.0
                </td>
            </tr> 
            <tr class="table--row">
                <td class="table--cell">jboost</td>
                <td class="table--cell">string</td>
                <td class="table--cell">Set to 0 to exclude addresses not allocated to a country </td>
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

   <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/ids</code></pre>

<h2>Sample Queries</h2>

<p><pre>7C Padfield Road, London, SE5 9AA</pre></p>

   <h2 class="saturn">Sample Output</h2>

   <pre><code>
{
    "apiVersion": "1.0.5",
    "matchDate": "2022-10-08",
    "response": {
        "addresses": [
            {
                "addressEntryId": "100000034563807",
                "confidenceScore": 94.1113
            },
            {
                "addressEntryId": "100000034563805",
                "confidenceScore": 11.2674
            },
            {
                "addressEntryId": "",
                "confidenceScore": 9.4509
            },
            {
                "addressEntryId": "100000034563806",
                "confidenceScore": 9.4503
            }
        ],
        "matchtype": "M"
    },
    "status": {
        "code": 200,
        "message": "Ok"
    },
    "errors": []
}
   </code></pre>
   