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
        <td class="table--cell"><a href="uprn/readme.md">/addresses/uprn/{uprn}</a></td>
        <td class="table--cell">
            Gets an address by UPRN.
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
    </tbody>
</table>