import React from 'react';
import createBrowserHistory from 'history/createBrowserHistory';
import {Route, Router, Switch} from 'react-router';
import {connect} from 'react-redux';
import {Badge} from 'reactstrap';

import Menu from '../components/general/Menu';
import Start from '../components/views/Start';
import RepoListView from '../components/views/repos/RepoListView';
import RepoArchiveListView from '../components/views/repos/RepoArchiveListView';
import ArchiveView from '../components/views/archives/ArchiveView';
import ConfigurationPage from '../components/views/config/ConfigurationPage';
import RestServices from '../components/views/develop/RestServices';
import JobMonitorView from '../components/views/jobs/JobMonitorView';
import {getRestServiceUrl, isDevelopmentMode} from '../utilities/global';
import LogPage from '../components/views/logging/LogPage';
import Footer from '../components/views/footer/Footer';
import {loadVersion} from '../actions';
import {getTranslation} from '../utilities/i18n';

class WebApp extends React.Component {

    history = createBrowserHistory();

    componentDidMount = () => {
        this.props.loadVersion();
        this.interval = setInterval(() => this.fetchJobStatistics(), 5000);
    };

    fetchJobStatistics = () => {
        fetch(getRestServiceUrl('jobs/statistics'), {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(response => response.json())
            .then(json => {
                this.setState({
                    statistics: json
                });
            })
            .catch();
    };

    render() {
        let jobsBadge = '';
        if (this.state && this.state.statistics && this.state.statistics.numberOfRunningAndQueuedJobs > 0) {
            jobsBadge = <Badge color="danger" pill>{this.state.statistics.numberOfRunningAndQueuedJobs}</Badge>;
        }
        let routes = [
            ['Start', '/', Start],
            ['Repositories', '/repos', RepoListView],
            ['Job monitor', '/jobmonitor', JobMonitorView, {badge: jobsBadge}],
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
