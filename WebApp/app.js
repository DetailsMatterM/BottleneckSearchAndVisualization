const express = require('express');
const bodyParser = require('body-parser');
const morgan = require('morgan');
const mqtt = require('mqtt');

const app = new express();
require('express-ws')(app);

const port = 3000;

// current topic  and address for MQTT client
let currentTopic = 'bottleneck';
// tcp://localhost:1883
// tcp://group3distributed.ddns.net:3189
let currentAddress = 'tcp://group3distributed.ddns.net:3189';

// connect to mqtt broker
let client = mqtt.connect(currentAddress, { clientId: 'visualizer_web' });

// local variables to hold data
let data;
let long;
let lat;

app.use(bodyParser.json());
app.use(morgan('dev'));
app.use(express.static('public'));

/**
 * Subscribes to a new Topic. Here's what happens:
 * 1. Extract and validate the topic(not empty or undefined).
 * 2. Unscribe from the old topic.
 * 3. Subscribe to the new topic.
 */
app.post('/change_topic', (req, res) => {
  const topic = req.body.topic;
  if (topic.length !== 0) {
    client.unsubscribe(currentTopic);
    client.unsubscribe('blindspot');
    client.unsubscribe('requestLoad');
    currentTopic = topic;

    client.subscribe(currentTopic);
    return res.json({ message: `Changed topic to: ${topic}` });
  }

  return res.json({ message: 'Topic can not be empty' });
});

/**
 * Change address(URL end-point) to which the MQTT client
 * is connected.
 * Once we change the address, we need to restart the MQTT client.
 */
app.post('/change_address', (req, res) => {
  const address = req.body.address;

  if (address.length !== 0) {
    client.end();
    currentAddress = address;
    client = mqtt.connect(currentAddress, { clientId: 'visualizer_web' });

    // todo duplicate code for onConnect and onMessage
    // NOTE: if this code is removed, the newly MQTT client has
    // no idea how to connect and listen to messages from the server.
    // maybe extract the logic into a separate function and
    // give client.on() a callback for that function.
    client.on('connect', () => {
      console.log('Connected to MQTT');

      // subscribe
      client.subscribe(currentTopic, { qos: 1 });
      client.subscribe('blindspot');
      client.subscribe('requestLoad');
    });

    client.on('message', (topic, message) => {
      data = JSON.parse(message.toString());
      // add the current topic to the data
      data['topic'] = topic;
      console.log(data);
    });

    return res.json({ message: `Changed address to: ${address}` });
  }

  return res.json({ message: 'Address can not be empty' });
});

client.on('connect', () => {
  console.log('Connected to MQTT');

  // subscribe
  client.subscribe(currentTopic, { qos: 1 });
  client.subscribe('blindspot');
  client.subscribe('requestLoad');
});

/**
 * React to messages from MQTT server.
 */
client.on('message', (topic, message) => {
  data = JSON.parse(message.toString());
  // add the current topic to the data
  data['topic'] = topic;
  console.log(data);
});

app.listen(port, () => console.log(`🚀 Running on localhost:${port}`));

// react to messages
app.ws('/', ws => {
  ws.on('message', msg => {
    sendData(ws);
    console.log(msg);
  });
});

/**
 * Send data over web-socket.
 * Do not send if we have duplicate data. The reason for this is
 * somethimes the random location can be same
 */
function sendData(ws) {
  setInterval(() => {
    if (
      data &&
      (long != data.location.longitude || lat != data.location.latitude)
    ) {
      ws.send(JSON.stringify(data));
      long = data.location.longitude;
      lat = data.location.latitude;
    }
  }, 10);
}
