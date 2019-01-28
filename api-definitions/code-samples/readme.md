<h1 class="jupiter">Code Samples</h1>

   <p>The following code examples should enable you to get started with using the API.</p>

   <h2>R</h2>

   <p>#API_URL# and #API_KEY# should be replaced with the actual values, appropriate to the environment and user.</p>

   <h3>/addresses example</h3>

   <pre><code># Load libraries
library(httr)
library(jsonlite)

# Call the API
call <- httr::GET("#API_URL#/addresses?input=7 Gate Reach, Exeter, EX2 6GA",
                  add_headers(Authorization = "#API_KEY#"))

# Retrieve the contents of the call
content <- httr::content(call, as = "text")

# Convert the contents from JSON to an R object
results <- jsonlite::fromJSON(content, flatten = TRUE)</code></pre>

<h2>Python</h2>

<p>The python example requires the <a href="http://docs.python-requests.org/en/master/" class="icon--external-link">requests</a> library</p>

<p>#API_URL# and #API_KEY# should be replaced with the actual values, appropriate to the environment and user.</p>

<p>The print function is included so the result can be seen in the console only, and should be removed for actual use.</p>

<h3>/addresses example</h3>

   <pre><code>import json
import requests

api_url = "#API_URL#"
endpoint = "/addresses"
search_term = "" #Only used for UPRN, Partial and Postcode endpoints
header = {"Content-Type": "application/json", 'Authorization': "#API_KEY#"}
params = "input=7 Gate Reach, Exeter, EX2 6GA&verbose=true"

request = api_url + endpoint + search_term


def get_addresses():

    try:
        response = requests.get(request, params=params, headers=header, timeout=1000000., verify=false)
        response.raise_for_status()
        results = json.loads(response.text)
    except requests.exceptions.HTTPError as error:
        if error.response.status_code == 400:
            results = json.loads(response.text)
        else:
            results = error
    except requests.exceptions.RequestException as error:
        results = error

    print(results)

    return results

if __name__ == '__main__':
    get_addresses()</code></pre>

