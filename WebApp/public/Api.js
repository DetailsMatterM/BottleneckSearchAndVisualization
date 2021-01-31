/*eslint no-undef:0*/
/*eslint-env browser*/

/**
 * This class is responsible for API which is used
 * to used to change the TOPIC and Address to which
 * the MQTT client is subscribed and connected.
 */
class ChangeController {
  constructor(url) {
    this.Api = axios.create({
      baseURL: url || 'http://localhost:3000'
    });
  }

  /**
   * Returns the TOPIC to which the MQTT client is subscribed.
   */
  static changeTopic() {
    const input = document.getElementById('subscription_topic');
    const topic = input.value;

    Controller.Api.post('/change_topic', { topic: topic })
      .then(response => {
        console.log(response.data);
      })
      .catch(error => {
        console.log(error);
      });

    console.log('topic: ', topic);
    input.value = '';
  }

  /**
   * Returns the Address to which the MQTT client is connected.
   */
  static changeBrokerURL() {
    const input = document.getElementById('broker_address');
    const address = input.value;

    Controller.Api.post('/change_address', { address: address })
      .then(response => {
        console.log(response.data);
      })
      .catch(error => {
        console.log(error);
      });

    console.log('url: ', address);
    input.value = '';
  }
}

const Controller = new ChangeController();
