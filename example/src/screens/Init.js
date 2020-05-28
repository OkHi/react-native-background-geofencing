import React from 'react';
import {CommonActions} from '@react-navigation/native';
import FullScreenLoader from '../components/FullScreenLoader';
import Repository from '../services/Repository';
import {wait} from '../services/Utils';
import {withContext} from '../context';

class InitScreen extends React.Component {
  repo = new Repository();

  shouldComponentUpdate() {
    return false;
  }

  async componentDidMount() {
    const {context} = this.props;
    const user = await this.repo.getUser();
    const geofences = await this.repo.getGeofences();
    const events = await this.repo.getGeofenceEvents();
    await wait(1500);
    if (user) {
      context.putUser(user);
      context.putGeofences(geofences);
      context.putGeofenceEvents(events);
      this.handleNavigation('Home');
    } else {
      this.handleNavigation('Login');
    }
  }

  handleNavigation = route => {
    const {navigation} = this.props;
    navigation.dispatch(
      CommonActions.reset({
        index: 0,
        routes: [
          {
            name: route,
          },
        ],
      }),
    );
  };

  render() {
    return <FullScreenLoader />;
  }
}

export default withContext(InitScreen);