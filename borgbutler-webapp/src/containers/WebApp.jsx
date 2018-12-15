import React from 'react';
import createBrowserHistory from 'history/createBrowserHistory';
import {Route, Router, Switch} from 'react-router';
import {connect} from 'react-redux';

import Menu from '../components/general/Menu';
import Start from '../components/views/Start';
import RepoListView from '../components/views/repos/RepoListView';
import RepoArchiveListView from '../components/views/repos/RepoArchiveListView';
import ArchiveView from '../components/views/repos/ArchiveView';
import ConfigurationPage from '../components/views/config/ConfigurationPage';
import RestServices from '../components/views/develop/RestServices';
import {isDevelopmentMode} from '../utilities/global';
import LogPage from '../components/views/logging/LogPage';
import Footer from '../components/views/footer/Footer';
import {loadVersion} from '../actions';
import {getTranslation} from '../utilities/i18n';

class WebApp extends React.Component {

    history = createBrowserHistory();

    componentDidMount = () => {
        this.props.loadVersion();
    };

    render() {
        let routes = [
            ['Start', '/', Start],
            ['Repositories', '/repos', RepoListView],
            [getTranslation('logviewer'), '/logging', LogPage],
            [getTranslation('configuration'), '/config', ConfigurationPage]
        ];

        if (isDevelopmentMode()) {
            routes.push(['Rest services', '/restServices', RestServices]);
        }

        return (
            <Router history={this.history}>
                <div>
                    <Menu routes={routes}/>
                    <div className={'container main-view'}>
                        <Switch>
                            {
                                routes.map((route, index) => (
                                    <Route
                                        key={index}
                                        path={route[1]}
                                        component={route[2]}
                                        exact
                                    />
                                ))
                            }
                            <Route path={'/repoArchives/:id'} component={RepoArchiveListView}/>
                            <Route path={'/archives/:repoId/:archiveId'} component={ArchiveView}/>
                        </Switch>
                    </div>
                    <Footer versionInfo={this.props.version}/>
                </div>
            </Router>
        );
    }
}

const mapStateToProps = state => ({
    version: state.version
});

const actions = {
    loadVersion
};

export default connect(mapStateToProps, actions)(WebApp);
