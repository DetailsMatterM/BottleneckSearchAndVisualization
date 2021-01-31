/*eslint no-undef:0*/
/*eslint-env browser*/

// add Map key here
const MAP_BOX_TOKEN =
  'pk.eyJ1IjoiZ3Vzc2luZ2dhIiwiYSI6ImNrMzhtbWVxZjA3NXgzYnBiZ210Z2p4MDYifQ.BdGExPt31DaokzAOWR-XJg';

const ws = new WebSocket('ws://localhost:3000');

ws.onopen = () => {
  ws.send('Client connected.');
};

mapboxgl.accessToken = MAP_BOX_TOKEN;

const BLIND_SPOT = 'blindspot';
const BOTTLE_NECKS = 'bottleneck';
const REQUEST_LOAD = 'requestLoad';

// map instance
let map;
// variables to hold different types of data to
// which the client can subscribe
let blindSpots = [];
let bottleNecks = [];
let requestLoads = [];

// Note: it doesn't work without onload wrapper.
window.onload = () => {
  if (!mapboxgl.supported()) {
    alert('Your browser does not support Mapbox GL');
  } else {
    // initialize map instance
    map = new mapboxgl.Map({
      container: 'map',
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [11.974374, 57.708581],
      zoom: 12
    });
  }

  // add zoom and rotation controls to the map.
  map.addControl(new mapboxgl.NavigationControl(), 'bottom-right');
  // add full-screen control to the map.
  map.addControl(new mapboxgl.FullscreenControl(), 'bottom-right');
  // add geolocate control to the map.
  map.addControl(
    new mapboxgl.GeolocateControl({
      positionOptions: {
        enableHighAccuracy: true
      },
      trackUserLocation: true
    }),
    'bottom-right'
  );

  // add geo-decoder to the map.
  map.addControl(
    new MapboxGeocoder({
      accessToken: mapboxgl.accessToken,
      mapboxgl: mapboxgl
    })
  );

  // checkboxes
  const blindSpotsButton = document.getElementById('blind-spots');
  const bottleNecksButton = document.getElementById('bottle-necks');
  const blindSpotsAndBottleNecksButton = document.getElementById(
    'blind-spots-and-bottle-necks'
  );
  // buttons to change topic and address
  const changeSubTopic = document.getElementById('change_subscription_topic');
  const changeBrokerURL = document.getElementById('change_broker_url');
  // button to clear data in variables
  const clearDataButton = document.getElementById('clear-data');

  blindSpotsButton.addEventListener('change', getBlindSpots);
  bottleNecksButton.addEventListener('change', getBottleNecks);
  blindSpotsAndBottleNecksButton.addEventListener(
    'change',
    getBlindSpotsAndBottleNecks
  );
  changeSubTopic.addEventListener('click', ChangeController.changeTopic);
  changeBrokerURL.addEventListener('click', ChangeController.changeBrokerURL);
  clearDataButton.addEventListener('click', clearData);

  // react to messages
  ws.onmessage = event => {
    const data = JSON.parse(event.data);

    // saves location data to individual variables
    saveLocationData(data);
  };

  /**
   * Pushes data in corresponding array variables depending on what
   * data we get from the server.
   * For example data for 'blindspot' topic should be pushed into
   * blindSpots array.
   */
  function saveLocationData(data) {
    const topic = data.topic;
    const location = data.location;

    if (topic === BLIND_SPOT) {
      let marker = createMarker(location.longitude, location.latitude);

      blindSpots.push(marker);
      blindSpotsButton.checked ? marker.addTo(map) : '';
    }
    if (topic === BOTTLE_NECKS) {
      let marker = createMarker(location.longitude, location.latitude);

      bottleNecks.push(marker);
      bottleNecksButton.checked ? marker.addTo(map) : '';
    }
    if (topic === REQUEST_LOAD) {
      let marker = createMarker(location.longitude, location.latitude);

      requestLoads.push(marker);
      blindSpotsAndBottleNecksButton.checked ? marker.addTo(map) : '';
    }

    console.log(blindSpots, bottleNecks, requestLoads);
  }

  /**
   * Creates an instance of a new Marker and returns it.
   * This is what gets pushed into individual arrays.
   * Creating individual Markers is handy because they can be
   * easily removed later from the map instance.
   */
  function createMarker(long, lat) {
    return new mapboxgl.Marker().setLngLat([long, lat]);
  }

  /**
   * Toogles whether location Markers for Blind Spots
   * should be shown on the map or not.
   */
  function getBlindSpots(e) {
    const checked = e.target.checked;

    blindSpots.forEach(marker => {
      checked ? marker.addTo(map) : marker.remove();
    });
  }

  /**
   * Toogles whether location Markers for Bottle Necks
   * should be shown on the map or not.
   */
  function getBottleNecks(e) {
    const checked = e.target.checked;

    bottleNecks.forEach(marker => {
      checked ? marker.addTo(map) : marker.remove();
    });
  }

  /**
   * Toogles whether location Markers for both Blind Spots and
   * Bottle Necks should be shown on the map or not.
   */
  function getBlindSpotsAndBottleNecks(e) {
    const checked = e.target.checked;

    requestLoads.forEach(marker => {
      checked ? marker.addTo(map) : marker.remove();
    });
  }

  /**
   * Clears data from individual arrays.
   */
  function clearData() {
    blindSpots.forEach(marker => {
      marker.remove();
    });
    bottleNecks.forEach(marker => {
      marker.remove();
    });
    requestLoads.forEach(marker => {
      marker.remove();
    });

    blindSpots = [];
    bottleNecks = [];
    requestLoads = [];
    console.log('Data cleared', blindSpots, bottleNecks, requestLoads);
  }

  // function getScenario1(e) {
  //   console.log('getting Scenario1');
  // }

  // function getScenario2(e) {
  //   console.log('getting Scenario2');
  // }
};
