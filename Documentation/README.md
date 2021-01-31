# dit355-group03-documentation

# Description

This repository contains documentation of software that helps in finding blindspots and bottlenecks in public transportation system located in the greater Gothenburg area. The Documentations are provided with formats of png and online word document on google.

## Requirements

* [Eclipse Mosquitto-Paho]
* [Trafic data]
* [Java, JavaScript]

## Documentations included


[1. Use Case Diagram](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/blob/main/Documentation/Use%20Case%20Diagram.png) <br>
2. Class Diagram <br>
&emsp; 2.1 [Android Class Diagram  ](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/blob/main/Documentation/ClassDiagram_Android.png)(ClassDiagram_Android.png) <br>
&emsp; 2.2 [Generator Class Diagram](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/blob/main/Documentation/ClassDiagram_Generator.png)  (ClassDiagram_Generator.png) <br>
&emsp; 2.3 [Creator Class Diagram](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/blob/main/Documentation/ClassDiagram_Creator.png) (ClassDiagram_Creator.png) <br>
&emsp; 2.4 [WebApp Class Diagram](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/blob/main/Documentation/ClassDiagram_WebApp.png) (ClassDiagram_WebApp.png) <br>
&emsp; 2.5 [Layered Class Diagram](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/blob/main/Documentation/N-tier%20Layered%20Class%20Diagram.png)(N-tier Layered Class Diagram.png) <br>
3. Component Diagram <br>
&emsp; 3.1 [Implementational Component Diagram](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/tree/main/Documentation#31-implementational-component-diagram-) <br>
4. Sequence Diagram <br>
&emsp; 4.1 [Pipe - filter](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/blob/main/Documentation/SequenceDiagram%20-%20Pipe-Filter.png) <br>
&emsp; 4.2 [Publish - subscribe](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/blob/main/Documentation/SequenceDiagram%20-%20Publish-Subscribe.png) <br>
5. [Deployment Diagram](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/blob/main/Documentation/DeploymentDiagram.png) <br>
6. Description of diagrams in link https://docs.google.com/document/d/13-js5eSS014D49NSvfx9Oh9xObibeI5s_VgdBMAKGi8/edit# <br>

 




<h2> 1. Use Case Diagram </h2>
The use case diagram shows the main actors, the City Planners and Västtrafik with the use cases that we have decided to work on. 
These are:
<ul>
<li>Find Blindspots</li>
<li>Show Blindspots</li>
<li>Find Bottlenecks</li>
<li>Show Bottlenecks</li>
<li>Plot Commuter request</li>
<li>Create Commuter request</li>
</ul>

Since the user can choose between Blindspots and Bottlenecks, we are using the shown structure of “Show Blindspot”/”Show Bottlenecks”. 
Both are extending “Get visual representation”, and they include “Find Blindspot”/”Find Bottleneck”, since there wouldn’t be data points to be plotted without this process. 
Every time the user gets a visual representation, the plotting of Commuter-Requests is involved, which includes, 
that these have been created by the application (Mock-up).

<h2>2.1 Android Class Diagram</h2>
<p>The main activity (named MapsActivity) receives JSON data via the MqttAndroidClient, by subscribing to specific topic(s). 
The main activity has two basic functionalities. These are, (1) subscribing to topics and listening to messages over the Mqtt broker and (2) generation of google map and plotting recieved messages and locations. 
It instantiates BlindSpot-Objects and StationLoad-Objects, based on the received JSON data.
The BlindSpot and StationLoad classes are used to extract the data received as a text via MQTT. They also allow the application to differentiate between the colors of the plotted markers and to access these data. The location, that is contained in these objects, is also used to animate the zoom on the displayed map.
</p>

<h2>2.2 Generator Class Diagram</h2>
<p>The Publisher instantiates the Generator which randomly generates data according to the requirements.</p>

<h2>2.3 Creator Class Diagram</h2>
<p>The Pipemanager receives data and sends it through the pipe. The Pipe sends the data through filters. The filters alter the data and return it back to the Pipe. The Creator receives the data from the Pipemanager. The TrafficInfo class is used to fetch data from the Västtrafik API. OauthToTraffic is used to authenticate data from the Västtrafik API.</p>

<h2>2.4 WebApp Class Diagram</h2>
<p>There are 3 main classes being used, api.js is responsible for API that is used to change the TOPIC and ADDRESS to which the MQTT client is subscribed and connected to.<br> Map.js is used for the map logic and applies the data from the publisher. App.js handles the MQTT connections and packages.</p>

<h2>2.5 Layered Class Diagram</h2>
<p>The TrafficInfo-Api, a Generator, and the Authenticator are located in the data access layer. TrafficInfo is connected to the pipeManager in the business layer. 
In the business logic layer the Creator class is connected to PipeManager, which through the Pipe is interacting with the filters BottleNeckFilter and BlindSpotFilter.  
The Creator publishes data through the MQTT broker in the cross-cutting layer and passes it to the application layer logic where the receiver is. 
The visualizers gets the data from the Receiver and displays it to the user.
</p>


<h2>3.1 Implementational Component Diagram </h2> 
<p>The implementational component diagram is intended to visualize how our components are used. 
<ul>
List of components in the diagram :
<li>FilteringSubsystem </li>
<li>BottleNeckFilter </li>
<li>BlindSpotFilter</li>
<li>CommunicationSubsystem</li>
<li>TrafficInfo</li>
<li>Pipe</li>
<li>Controller Subsystem</li>
<li>PipeManager</li>
<li>Middleware</li>
<li>Creator</li>
<li>Generator</li>
<li>Visualizer Web</li>
<li>Visualizer Android</li>
The pipeManager transfers data to the Pipe, which transfers it to the appropriate components in the filtering subsystem that returns the filtered data. The pipe passes the data onto the creator component and broadcasts it to the MQTT broker. 
The Generator component, an external entity that runs independently of other systems is mocking the requests of the commuters. It is broadcasting the generated data via MQTT.
The Visualizers are external entities that run independently of the others systems. They are subscribed to one or several topics and receive data over MQTT.
</ul>
</p>

<h2>3.2 Conceptual Component Diagram </h2> 
<p>The conceptual component diagram allows to convey the initial plan and the idea for the system-components. 
<ul>
List of components in the diagram:
<li>Filtering Subsystem</li>
<li>Communication Subsystem</li>
<li>Controller Subsystem</li>
<li>Middleware</li>
<li>Creator</li>
<li>Generator</li>
<li>Visualizer Web</li>
<li>Visualizer Android</li>
The communication subsystem is responsible for transferring data over pipes through the system.
The Generator component, an external entity that runs independently of other system is mocking the requests of the commuters. It is broadcasting the generated data via MQTT.
The Controller subsystem transfers data to the communication subsystem, which transfers it to the Filtering Subsystem. In the next step, the manipulated data is transferred to the Creator component over the communication subsystem. The creator receives the filtered data and broadcasts it to the middleware.
The Visualizers are external entities that run independently of the others systems. They are subscribed to one or several topics and receive data over MQTT. 
</ul>
</p>
<h2> 4.1 Sequence Diagram Pipe - filter </h2>
<p>The pipeManager which we consider an actor, transfers data through the Pipe to a filter. Depending on what filter it goes through, returns data from either BottleNeck or BlindSpot filter. The Pipe instantiates an object of type Creator, which calls emitter() to broadcast manipulated data from the Pipe on a given Topic. 
</p>
<h2> 4.2 Sequence Diagram  Publisher - Subscriber </h2>
<p>
The pipe will transfer the filtered data to the creator, which will connect to the middleware during its instantiation. When the connection is not successful, an exception will be thrown. When the connection is successful, the creator will publish the data under a specified topic. The middleware will broadcast the data as a message under the given topic  to the visualizer.
</p>

<h2>5. Deployment Diagram</h2>
<p>The blue colored boxes are nodes (including external systems). The green boxes are components. The pink boxes are devices. The beige boxes are subsystems. 
The Västraffik server and the Map API server are external entities, and are represented in the diagram for completion.
The nodes are connected to other components and services, using TCP or MQTT. The node creator, running on a PC, is communicating with the Västtrafik server and the MQTT broker. The Generator-component is sending data to the creator node through MQTT. 
The Visualizer components are implemented on different devices, namely PC and Android devices. They are also connected to the Map API node. These are graphically showing the data they received over MQTT to an end user. 
</p>
