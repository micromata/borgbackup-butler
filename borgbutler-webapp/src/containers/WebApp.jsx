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
import CreateRepoPage from "../components/views/repos/CreateRepoPage";

class WebApp extends React.Component {

    history = createBrowserHistory();

    componentDidMount = () => {
        this.props.loadVersion();
        this.interval = setInterval(() => this.fetchSystemInfo(), 5000);
    };

    fetchSystemInfo = () => {
        fetch(getRestServiceUrl('system/info'), {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(response => response.json())
            .then(json => {
                this.setState({
                    systemInfo: json
                });
            })
            .catch();
    };

    render() {
        let jobsBadge = '';
        const statistics = (this.state && this.state.systemInfo) ? this.state.systemInfo.queueStatistics : null;
        if (statistics && statistics.numberOfRunningAndQueuedJobs > 0) {
            jobsBadge = <Badge color="danger" pill>{statistics.numberOfRunningAndQueuedJobs}</Badge>;
        }
        let configurationBadge = '';
        if (this.state && this.state.systemInfo && !this.state.systemInfo.configurationOK) {
            configurationBadge = <Badge color="danger" pill>!</Badge>;
        }
        let routes = [
            ['Start', '/', Start],
            ['Repositories', '/repos', RepoListView],
            ['Job monitor', '/jobmonitor', JobMonitorView, {badge: jobsBadge}],
            [getTranslation('logviewer'), '/logging', LogPage],
            [getTranslation('configuration'), '/config', ConfigurationPage, {badge: configurationBadge}]
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
                            <Route path={'/repoArchives/:id/:displayName'} component={RepoArchiveListView}/>
                            <Route path={'/archives/:repoId/:archiveId'} component={ArchiveView}/>
                            <Route path={'/repo/new'} component={CreateRepoPage}/>
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
