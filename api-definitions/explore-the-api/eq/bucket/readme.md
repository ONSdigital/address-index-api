<h1>/addresses/eq/bucket</h1>

<p>Search for addresses in a postcode_street_town grouping (called a bucket).</p>

<h2>Request</h2>

<p><code>GET /addresses/eq/bucket</code></p>
   

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
            <td class="table--cell">postcode</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the postcode part of the bucket (e.g. 'HA4 8RG').</td>
            <td class="table--cell">
                   Optional
                   <br>Default: *
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">streetname</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the street part of the bucket (e.g. 'Acacia Avenue')..</td>
            <td class="table--cell">
                Optional
                <br>Default: *
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">townname</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the town part of the bucket (e.g. 'Ruislip').</td>
            <td class="table--cell">
                Optional
                <br>Default: *
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

<p>?postcode=EX4%201AA&streetname=Cowick%20Street&townname=Exeter</p>

   <h2>Sample Output</h2>
   
   <pre><code>
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "19",
  "response": {
    "postcode": "EX4 1AA",
    "streetname": "Cowick Street",
    "townname": "Exeter",
    "addresses": [
      {
        "uprn": "100041044587",
        "formattedAddress": "British Rail Staff Association (w R), 172 Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      },
      {
        "uprn": "10013036606",
        "formattedAddress": "Great Western Railway Staff Association Club, 172 Cowick Street, Exeter, EX4 1AA",
        "addressType": "NAG"
      },
      {
        "uprn": "100041141461",
        "formattedAddress": "Kings Arms, 173 Cowick Street, Exeter, EX4 1AA",
        "addressType": "NAG"
      },
      {
        "uprn": "10013048037",
        "formattedAddress": "Kings Arms Inn, 173 Cowick Street, Exeter, EX4 1AA",
        "addressType": "NAG"
      },
      {
        "uprn": "10013036607",
        "formattedAddress": "The Kings, 173 Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      },
      {
        "uprn": "100041044589",
        "formattedAddress": "Pawelek, 174-175 Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      },
      {
        "uprn": "10013036608",
        "formattedAddress": "Cobblers, 177 Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      },
      {
        "uprn": "100041044591",
        "formattedAddress": "Ascot Jewellers, 178 Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      },
      {
        "uprn": "100041044594",
        "formattedAddress": "Exebridge Cafe, 180 Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      },
      {
        "uprn": "10013036610",
        "formattedAddress": "Exebridge Cafe, 180 Cowick Street, Exeter, EX4 1AA",
        "addressType": "NAG"
      },
      {
        "uprn": "10013036611",
        "formattedAddress": "The Herbal Clinic, 181 Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      },
      {
        "uprn": "100041044595",
        "formattedAddress": "Hidden Hearing LTD, 182 Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      },
      {
        "uprn": "10013036612",
        "formattedAddress": "Hidden Hearing LTD, 182 Cowick Street, Exeter, EX4 1AA",
        "addressType": "NAG"
      },
      {
        "uprn": "100041044596",
        "formattedAddress": "Nutters Hairdressers, 183 Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      },
      {
        "uprn": "10013036613",
        "formattedAddress": "Nutters Hairdressers, 183 Cowick Street, Exeter, EX4 1AA",
        "addressType": "NAG"
      },
      {
        "uprn": "100041044597",
        "formattedAddress": "183A Cowick Street, Exeter, EX4 1AA",
        "addressType": "PAF"
      }
    ],
    "filter": "",
    "epoch": "",
    "limit": 100,
    "offset": 0,
    "total": 16,
    "maxScore": 0
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": [
   ]
}
</code></pre>