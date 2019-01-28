<h1 class="jupiter">/addresses/partial</h1>



<p>Search by partial address (for type ahead).</p>

<h2>Request</h2>

<p><code>GET /addresses/partial</code></p>



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
        <td class="table--cell">startboost</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Boost results where the input string appears at the start of the address (0 = no boost).</td>
        <td class="table--cell">
            Optional
            <br>Default: 2
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

<p><pre>7 Gate Re</pre></p>
<p><pre>4 EX26GA</pre></p>

   <h2>Sample Output</h2>



   <pre><code>{
    &#34;apiVersion&#34;: &#34;v_3d37d4ca49a9f451284fca57185aa5df7ba30658&#34;,
    &#34;dataVersion&#34;: &#34;39&#34;,
    &#34;errors&#34;: [],
    &#34;response&#34;: {
        &#34;addresses&#34;: [
            {
                &#34;classificationCode&#34;: &#34;RD&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;formattedAddressNag&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;formattedAddressPaf&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 293535,
                    &#34;latitude&#34;: 50.705948,
                    &#34;longitude&#34;: -3.5091076,
                    &#34;northing&#34;: 90677
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;0&#34;,
                &#34;underlyingScore&#34;: 5.85916805267334,
                &#34;uprn&#34;: &#34;10023122457&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;
            },
            {
                &#34;classificationCode&#34;: &#34;RD&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;7 North Gate House, Northernhay Gate, Exeter, EX4 3SA&#34;,
                &#34;formattedAddressNag&#34;: &#34;7 North Gate House, Northernhay Gate, Exeter, EX4 3SA&#34;,
                &#34;formattedAddressPaf&#34;: &#34;7 North Gate House, Northernhay Gate, Exeter, EX4 3SA&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 291917,
                    &#34;latitude&#34;: 50.725525,
                    &#34;longitude&#34;: -3.532665,
                    &#34;northing&#34;: 92889
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;10013050564&#34;,
                &#34;underlyingScore&#34;: 2.2647452354431152,
                &#34;uprn&#34;: &#34;10013050571&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;7 North Gate House, Northernhay Gate, Exeter, EX4 3SA&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;7 North Gate House, Northernhay Gate, Exeter, EX4 3SA&#34;
            },
            {
                &#34;classificationCode&#34;: &#34;RD&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;7 Summerland Gate, Belgrave Road, Exeter, EX1 2NP&#34;,
                &#34;formattedAddressNag&#34;: &#34;7 Summerland Gate, Belgrave Road, Exeter, EX1 2NP&#34;,
                &#34;formattedAddressPaf&#34;: &#34;7 Summerland Gate, Belgrave Road, Exeter, EX1 2NP&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 292692,
                    &#34;latitude&#34;: 50.726498,
                    &#34;longitude&#34;: -3.5217159,
                    &#34;northing&#34;: 92981
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;10013045075&#34;,
                &#34;underlyingScore&#34;: 2.2467124462127686,
                &#34;uprn&#34;: &#34;10013045082&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;7 Summerland Gate, Belgrave Road, Exeter, EX1 2NP&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;7 Summerland Gate, Belgrave Road, Exeter, EX1 2NP&#34;
            },
            {
                &#34;classificationCode&#34;: &#34;RD&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;5-7 Palace Gate, Exeter, EX1 1JA&#34;,
                &#34;formattedAddressNag&#34;: &#34;5-7 Palace Gate, Exeter, EX1 1JA&#34;,
                &#34;formattedAddressPaf&#34;: &#34;5 Palace Gate, Exeter, EX1 1JA&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 292081,
                    &#34;latitude&#34;: 50.721252,
                    &#34;longitude&#34;: -3.5302022,
                    &#34;northing&#34;: 92410
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;0&#34;,
                &#34;underlyingScore&#34;: 1.1172213554382324,
                &#34;uprn&#34;: &#34;10013042855&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;5-7 Palace Gate, Exeter, EX1 1JA&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;5 Palace Gate, Exeter, EX1 1JA&#34;
            },
            {
                &#34;classificationCode&#34;: &#34;OR03&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;PO BOX 797, Summerland Gate, Belgrave Road, Exeter, EX1 9UN&#34;,
                &#34;formattedAddressNag&#34;: &#34;PO BOX 797, Summerland Gate, Belgrave Road, Exeter, EX1 9UN&#34;,
                &#34;formattedAddressPaf&#34;: &#34;PO BOX 797, Exeter, EX1 9UN&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 292653,
                    &#34;latitude&#34;: 50.72631,
                    &#34;longitude&#34;: -3.5222623,
                    &#34;northing&#34;: 92961
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;0&#34;,
                &#34;underlyingScore&#34;: 0.9661685824394226,
                &#34;uprn&#34;: &#34;10092760044&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;PO BOX 797, Summerland Gate, Belgrave Road, Exeter, EX1 9UN&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;PO BOX 797, Exeter, EX1 9UN&#34;
            },
            {
                &#34;classificationCode&#34;: &#34;OR03&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;PO BOX 79, Summerland Gate, Belgrave Road, Exeter, EX4 9YT&#34;,
                &#34;formattedAddressNag&#34;: &#34;PO BOX 79, Summerland Gate, Belgrave Road, Exeter, EX4 9YT&#34;,
                &#34;formattedAddressPaf&#34;: &#34;PO BOX 79, Exeter, EX4 9YT&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 292653,
                    &#34;latitude&#34;: 50.72631,
                    &#34;longitude&#34;: -3.5222623,
                    &#34;northing&#34;: 92961
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;0&#34;,
                &#34;underlyingScore&#34;: 0.9661685824394226,
                &#34;uprn&#34;: &#34;10015084719&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;PO BOX 79, Summerland Gate, Belgrave Road, Exeter, EX4 9YT&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;PO BOX 79, Exeter, EX4 9YT&#34;
            },
            {
                &#34;classificationCode&#34;: &#34;OR03&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;PO BOX 751, Summerland Gate, Belgrave Road, Exeter, EX1 9RU&#34;,
                &#34;formattedAddressNag&#34;: &#34;PO BOX 751, Summerland Gate, Belgrave Road, Exeter, EX1 9RU&#34;,
                &#34;formattedAddressPaf&#34;: &#34;PO BOX 751, Exeter, EX1 9RU&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 292653,
                    &#34;latitude&#34;: 50.72631,
                    &#34;longitude&#34;: -3.5222623,
                    &#34;northing&#34;: 92961
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;0&#34;,
                &#34;underlyingScore&#34;: 0.9490375518798828,
                &#34;uprn&#34;: &#34;10015184454&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;PO BOX 751, Summerland Gate, Belgrave Road, Exeter, EX1 9RU&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;PO BOX 751, Exeter, EX1 9RU&#34;
            },
            {
                &#34;classificationCode&#34;: &#34;R&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;Housemasters Flat, 5-7 Palace Gate, Exeter, EX1 1JA&#34;,
                &#34;formattedAddressNag&#34;: &#34;Housemasters Flat, 5-7 Palace Gate, Exeter, EX1 1JA&#34;,
                &#34;formattedAddressPaf&#34;: &#34;&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 292081,
                    &#34;latitude&#34;: 50.721252,
                    &#34;longitude&#34;: -3.5302022,
                    &#34;northing&#34;: 92410
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;10013042855&#34;,
                &#34;underlyingScore&#34;: 0.9490375518798828,
                &#34;uprn&#34;: &#34;10013047538&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;Housemasters Flat, 5-7 Palace Gate, Exeter, EX1 1JA&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;&#34;
            },
            {
                &#34;classificationCode&#34;: &#34;OR03&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;PO BOX 769, Summerland Gate, Belgrave Road, Exeter, EX1 9TL&#34;,
                &#34;formattedAddressNag&#34;: &#34;PO BOX 769, Summerland Gate, Belgrave Road, Exeter, EX1 9TL&#34;,
                &#34;formattedAddressPaf&#34;: &#34;PO BOX 769, Exeter, EX1 9TL&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 292653,
                    &#34;latitude&#34;: 50.72631,
                    &#34;longitude&#34;: -3.5222623,
                    &#34;northing&#34;: 92961
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;0&#34;,
                &#34;underlyingScore&#34;: 0.9490375518798828,
                &#34;uprn&#34;: &#34;10015493380&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;PO BOX 769, Summerland Gate, Belgrave Road, Exeter, EX1 9TL&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;PO BOX 769, Exeter, EX1 9TL&#34;
            },
            {
                &#34;classificationCode&#34;: &#34;OR03&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;formattedAddress&#34;: &#34;PO BOX 714, Summerland Gate, Belgrave Road, Exeter, EX1 9QH&#34;,
                &#34;formattedAddressNag&#34;: &#34;PO BOX 714, Summerland Gate, Belgrave Road, Exeter, EX1 9QH&#34;,
                &#34;formattedAddressPaf&#34;: &#34;PO BOX 714, Exeter, EX1 9QH&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 292653,
                    &#34;latitude&#34;: 50.72631,
                    &#34;longitude&#34;: -3.5222623,
                    &#34;northing&#34;: 92961
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;0&#34;,
                &#34;underlyingScore&#34;: 0.9373786449432373,
                &#34;uprn&#34;: &#34;10015143499&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;PO BOX 714, Summerland Gate, Belgrave Road, Exeter, EX1 9QH&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;PO BOX 714, Exeter, EX1 9QH&#34;
            }
        ],
        &#34;endDate&#34;: &#34;&#34;,
        &#34;epoch&#34;: &#34;&#34;,
        &#34;filter&#34;: &#34;&#34;,
        &#34;historical&#34;: true,
        &#34;input&#34;: &#34;7 gate&#34;,
        &#34;limit&#34;: 10,
        &#34;maxScore&#34;: 3.859168,
        &#34;offset&#34;: 0,
        &#34;startDate&#34;: &#34;&#34;,
        &#34;total&#34;: 28,
        &#34;verbose&#34;: false
    },
    &#34;status&#34;: {
        &#34;code&#34;: 200,
        &#34;message&#34;: &#34;Ok&#34;
    }
}</code></pre>