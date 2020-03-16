<h1>Address Index Simple Tester</h1>

The Address Index Simple Tester allows users to use the various functions available via the API, providing an input request and receiving a response from the API.

It assumes you have access to at least one API endpoint, and this URL is supplied on-screen.

<h2>Functionality</h2>

The Tester has 8 functions: 

<ol>
<li><strong>Version:</strong> Returns version information for the API and the data (AddressBase).</li>
<li><strong>Single Match:</strong> Returns a ranked list of addresses matching the search query in the specified format.</li>
<li><strong>Partial:</strong> Returns addresses matching an incomplete address string (For typeahead).</li>
<li><strong>Random:</strong> Returns a random address.</li>
<li><strong>Postcode:</strong> Returns a sorted list of addresses in a postcode.</li>
<li><strong>UPRN:</strong> Returns a single address, identified by its UPRN.</li>
<li><strong>EQ:</strong> Custom endpoint for EQ combines postcode and partial.</li>
<li><strong>Bulk:</strong> Will process all BulkQuery items in the BulkBody returns reduced information on found addresses (UPRN, formatted address).</li>
</ol>

<h2>Using the Simple Tester</h2>

The Simple Tester is a self-contained HTML file located at api-definitions/simple-tester/ai-demo.html, and can be run as a local file in Internet Explorer.

Each function has its own tab, and is accessed by clicking the tab.

Your API key, if required, needs to be set inside the code, as does the verbose parameter (defaults to true, set to false for concise respsone).

Where an input is required from the user, a default sample input is provided which will return a response, however the input can be changed.