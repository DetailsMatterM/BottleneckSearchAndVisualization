# Transport Support Visualizer (Android App) <h1>

# Description <h4>

This application visualizes the data it receives over it's subscription on a map.
It allows to subscribe to different topics and will handle the received data in
different ways.

# Prerequirements <h4>

- installed Android Studio (prefereably recent version or above 3.5.0)
- installed maven 3.5 or above


# Features <h4>

- When the applicaiton is started, it will show the center of Gothenburg
- The map displays broadcasted blindspots and bottlenecks
- It is possible to enter custom topics
- It is possible to enter custom broker IPs
- The application can generate a heatmap from the received data
- The wrench button is red, if the connection has been made and grey if thats not the case.
- If the connection is lost, the application will automatically resume to plot points when it has re-connected.

# User Guide <h4>

**How to run the application:** <br>
Upon starting the application a connection to the default MQTT broker will be made automatically. When the connection is successfully made a message will inform the user.
The background color of the settings button turns red if connected and grey if disconnected. 

**Settings and information:** <br>
The settings menu can be accessed via clicking on the settings button (red button with wrench symbol).<br>
After the settings menus has been opened several settings are available:

- Auto View Mode <br>
The view mode can be toggled between auto or manual. The auto mode will adjust the view to show newly arrived plots. This mode stops after a defined certain number of plotted points
to increase the overview. 
- Bottleneck View Choice <br>
This seting allows to toggle between the use of a heatmap or markers to display the bottlenecks.
- Subscription Topics <br>
Here different topics can be subscribe to. The user is able to chose between Blindspots, Supply Load, Request Load and a Custom Topic. When chosen to activate, the custom topic will ask the user to enter the name of the desired topic.
If the format of the given Topic is correct, the application will subscribe to it automatically.
- Broker Choice <br>
Allows to change the broker ip. By clicking on Edit Broker Path, the user is given the option to enter a custom broker path.
- Connection Choice <br>
This settings allows the manual connect or disconnect.
- Data Option <br>
After chosing this option, the application will delete all received data, the plotted points will be removed.. 



