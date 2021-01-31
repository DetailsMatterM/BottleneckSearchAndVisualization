## Description

This application is used to visualize requests from commuters on public transportation on a map.

### Getting started

1. Install packages with:
   ```bash
   yarn install or npm install
   ```
2. Add **MapBox Key** at `MAP_BOX_TOKEN` variable in `public/map.js` file.
3. Run `yarn dev` or `npm run dev` for development.
4. Visit `localhost:3000`.

## Functionality

There are three main components:

1. `app.js` file is the entry point for the `server`.
   It's responsible for connecting and listening to messages from an `MQTT` server.
   This is where a `web-socket` houses, which sends each message received from the `MQTT server` over the wire to individual
   clients that are connected.
   It also contains two `API's`, of `/change_topi` and `/change_address` end-points which lets the `client` to change the `Subscription Topic` and the `Broker Address` respectively.
2. `map.js` file is responsible for rendering the `Map` in the browser. For the `Map` to function user must pass give it a `MAP_BOX_TOKEN` which can be found on [Mapbox website](https://account.mapbox.com).
   A web-socket reacts to the location messages(JSON type) received from the server and parses it and saves individual messages to their respective array variables, namely `blindSpots`, `bottleNecks` and `request loads`.

   Users can choose which `Location Marker` to show on the map by toggling checkboxes. Users may also choose to change the `topic` and the `Broker address` to which the `MQTT client` is subscribed and connected.

3. `Api.js` file is responsible for actions performed when a user wants to change the subscription topic and the broker address on the client.
