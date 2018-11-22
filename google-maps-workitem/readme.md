# Requirements
Google Maps Web Service request requires an API key or client ID. API keys are freely available with a Google Account at developers.google.com/console. The type of API key you need is a Server key.

To get an API key:

Visit developers.google.com/console and log in with a Google Account.
Select one of your existing projects, or create a new project.
Enable the API(s) you want to use. 
The Java Client for Google Maps Services accesses the following APIs:
1. Directions API
2. Distance Matrix API
3. Elevation API
4. Geocoding API
4. Maps Static API
5. Places API
6. Roads API
7. Time Zone API
8. Create a new Server key.

Use your created key to pass to your used workitems constructor.


# Geocoding Workitem parameters
The geocoding workitem includes some input parameters which should be explained:

1. SearchType - geocoding info can be searched by 3 different types: 
    * "byaddress" - search location by address
    * "byplaceid" - search by google maps specific place id
    * "bylatlong" - search location by lat and long values. This can be entered with a comma-separated string value,
    for example "-33.8674869, 151.2069902"
    
2. LocationType - can be specified to further define the location type. If not specified will be blank. Can be values:
    * "rooftop"
    * "approximate"
    * "range"
    * "center"
    
# Directions Workitem parameters
The directions workitem includes some input parameters which should be explained:

1. Mode - the driving mode. If not specified defaults to "driving"
    * "walking"
    * "bicycling"
    * "transit"
    * "driving"
    
2. Avoid - can be specified to define what to avoid for directions. If not specified it is blank. Values can be:
    * "tolls"
    * "highways"
    * "ferries"
    
2. Units - Can be specified to change units of measure. Default if not specified is metric units. Can be values:
    * "metric"
    * "imperial"
    
    