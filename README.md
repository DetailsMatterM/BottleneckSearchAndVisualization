# BottleneckSearchAndVisualization
This project has been created during the course DIT355 Distributed Systemdevelopment.
It is intended to analyse the public transportation in the greater Gothenburg area for bottlenecks and blindspots.
To do so, it utilizes a Västtrafik api, furthermore it contains a Generator that can be used to simulate certain amounts of traffic within the city.
Utilizing the Pipe and Filter architecture pattern, data is manipulated and then broadcasted over Mqtt to subscribers, namely an Android App, as well as a Web App. 
Those subscribe to a set of topics and visualize the data they receive accordingly. 
More extensive documentation can be found in the [Documentation](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/tree/main/Documentation) section. <br> 

This project was devevloped on Gitlab and then transfered to Github

# The project consists of
* [Publisher](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/tree/main/Publisher) <br>
* [Subscriber](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/tree/main/Subscriber) <br>
* [Generator](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/tree/main/Generator) <br>
* [AndroidApp](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/tree/main/AndroidApp) <br>
* [WebApp](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/tree/main/WebApp) <br>
* [Documentation](https://github.com/DetailsMatterM/BottleneckSearchAndVisualization/tree/main/Documentation) <br>

# Team
* Eyuell Hailemichael Gebremedhin
* Fredrik Ullman
* Gagandeep Singh
* Haider Ali
* Miakel Köse Jansson
* Moritz Denke
