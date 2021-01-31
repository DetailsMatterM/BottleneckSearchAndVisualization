# Description 

This application is receiving and manipulating data sent from external entities. It is intended to pass data through an Mqtt procotol to a receiver that visualizes commuter requests in public transportation. 

# PipeManager

1.  Creates a connection to the MQTT server (createConnection ())
2.  Subscribes to the topic "external" (subscribeToMessages())
3.  Waits for any message on topic "external"
4.  If any message (JSON Object) is received, it's broken down in 2 JSON objects, Origin and Destination.
5.  2 different ThreadPools are created or used for managing the sequence of actions which should happen Asynchronously and in parallel to increase the performance during overload of received requests.  
    
    ###ThreadPool's Sequence
    
        1.  Sends Origin data to the Bottleneck/BlindSpot Filter and waits to get the filtered Data.
        2.  Sends Destination data to the Bottleneck/BlindSpot Filter and waits to get the filtered Data.
        3.  Checks if there is any filtered data, which means if it is different than "Null".
        4.  Only if there is any filtered data, it is forwarded to the Creator for the Publishing to the Visualizer.
        5.  In case the Filtered Data is from Bottleneck filter, a further checkup on the number of vehicles is done to decide if it should be delivered to the Creator or not. 

6.  From point 3-5 are continuously repeated. 


# Pipe

1.  For filtering, this receives Origin and Destination data from the PipeManager and forwards them to the filters. 
2.  It takes the filtered data from the filters and delivers it back to the PipeManager. 
3.  For sending the filtered data to the Visualizers, it receives the filtered data from the PipeManager and forwards it to the "Creator". 

#BottleneckFilter

1.  Receives data related to Origin or Destination and process the Bottleneck functionality.
2.  Bottleneck is calculated through :

    
*   Searches the first closest Stops' Information (getFirstNearbyStop()) from TrafficInfo and returns it to the filter asking the information.
*   If the returned Stop Information is different than Null, it provides the stops' information to __countTransportationInArea()__ in TrafficInfo which returns the number of Transportation in the provided "stop" Location.
*   Depending on the number of Transportation, If the number is more than 4 than it is considered as a Bottleneck and it returns an JSON Object containing location information and Number of Vehicles. 
        i.e. __{"nVehicles":10,"location":{"latitude":"57.681200","longitude":"11.965182"}}__
*   In case the number of transportation is lesser than 5, Null is returned.
    
#BlindSpotFilter

1. Receives data related to Origin or Destination and process the BlindSpot functionality.
2. BlindSpot is calculated through :

    2.1. Searches first if there are any stops around the location (origin or Destination) provided, by using nearbyStops() in TrafficInfo and returns the number of stops within 1km.
    
 #TrafficInfo
 
 1. Establishes a connection with the Reseplanerare (Västtrafik) Api. 
 2. Provides the functionality to retrieve the number of Transportation in a specific location by using :filterBottleNeck() 
 3. Provides the functionality to retrieve the closest stops information to the location provided by using : getFirstNearbyStop()
 4. Provides the functionality to retrieve the number of stops around a specific location provided by the filter b using : nearbyStops()
 
 #Oauth2Traffic
 
 1. Contains the keys and credentials for creating the OAUTH2 token to access the Reseplanerare (Västtrafik) Api.
 2. Provides the functionality to retrieve the latest active token assigned by the Latter Api by using the method getClientCredentials().

#Creator

1.  Establishes the connection with the MQTT server when its first initialized in the Pipe.
2.  Receives data to deliver or publish along with the "topic" from the Pipe component by using the method : emitter()
3.  Publishes/Sends the JSON Object (Message/data) to the topic received previously, to the MQTT server by using the method : sendMessage()