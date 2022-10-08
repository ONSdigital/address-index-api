<h1>Explore the API</h1>

<p>The ONS Address Index API lets you retrieve the official version of addresses in England and Wales. The data is currently updated on a 6 week rolling basis.</p>

<h2>Principle Endpoints</h2>

<table class="table">
    <thead class="table--head">
    <th scope="col" class="table--header--cell">Method</th>
    <th scope="col" class="table--header--cell">Endpoint</th>
    <th scope="col" class="table--header--cell">Description</th>
    </thead>
    <tbody>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="addresses/readme.md">/addresses</a></td>
        <td class="table--cell">
            Search for an address.
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="partial/readme.md">/addresses/partial</a></td>
        <td class="table--cell">
            Search by partial address (for type ahead).
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="postcode/readme.md">/addresses/postcode/{postcode}</a></td>
        <td class="table--cell">
            Search for an address by postcode.
        </td>
    </tr>
        <tr class="table--row">
            <td class="table--cell">GET</td>
            <td class="table--cell"><a href="groupedpostcode/readme.md">/addresses/groupedpostcode/{postcode}</a></td>
            <td class="table--cell">
                Search for postcodes matching a part postcode pattern.
            </td>
        </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="random/readme.md">/addresses/random</a></td>
        <td class="table--cell">
            Search for a random address.
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="uprn/readme.md">/addresses/uprn/{uprn}</a></td>
        <td class="table--cell">
            Gets an address by UPRN.
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">POST</td>
        <td class="table--cell"><a href="mutliuprn/readme.md">/addresses/multiuprn</a></td>
        <td class="table--cell">
            Gets addresses from an array of UPRNs.
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">POST</td>
        <td class="table--cell"><a href="bulk/readme.md">/bulk</a></td>
        <td class="table--cell">
            Runs a batch of up to 30000 addresses.
        </td>
    </tr>
   </tbody>
</table>

<h2>Custom Endpoints</h2>

<table class="table">
    <thead class="table--head">
    <th scope="col" class="table--header--cell">Method</th>
    <th scope="col" class="table--header--cell">Endpoint</th>
    <th scope="col" class="table--header--cell">Description</th>
    </thead>
    <tbody>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="ids/readme.md">/addresses/ids</a></td>
        <td class="table--cell">
            Custom endpoint for Integrated Data Service.
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="eq/readme.md">/addresses/eq</a></td>
        <td class="table--cell">
            Custom endpoint for Electronic Questionnaire.
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="eq/uprn/readme.md">/addresses/eq/uprn/{uprn}</a></td>
        <td class="table--cell">
            Gets an address by UPRN (EQ version).
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="rh/partial/readme.md">/addresses/rh/partial</a></td>
        <td class="table--cell">
            Search by partial address for type ahead (RH Version).
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="rh/postcode/readme.md">/addresses/rh/postcode/{postcode}</a></td>
        <td class="table--cell">
            Search for an address by postcode (RH version).
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell"><a href="rh/uprn/readme.md">/addresses/rh/uprn/{uprn}</a></td>
        <td class="table--cell">
            Gets an address by UPRN (RH version).
        </td>
    </tr>
  </tbody>
</table>


<h2>Supplementary Endpoints</h2>

<table class="table">
    <thead class="table--head">
    <th scope="col" class="table--header--cell">Method</th>
    <th scope="col" class="table--header--cell">Endpoint</th>
    <th scope="col" class="table--header--cell">Description</th>
    </thead>
    <tbody>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell">/classifications</td>
        <td class="table--cell">
            Return a list of available classification codes
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell">/codelists</td>
        <td class="table--cell">
            Return a list of codelists supported by the API
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell">/custodians</td>
        <td class="table--cell">
            Return a list of available local custodian codes
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell">/logicalstatuses</td>
        <td class="table--cell">
            Return a list of available logical status values
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell">/sources</td>
        <td class="table--cell">
            Return a list of available external source codes
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell">/version</td>
        <td class="table--cell">
            Get version information.
        </td>
    </tr>
    <tr class="table--row">
        <td class="table--cell">GET</td>
        <td class="table--cell">/epochs</td>
        <td class="table--cell">
            Get list of available epochs.
        </td>
    </tr>
    </tbody>
</table>