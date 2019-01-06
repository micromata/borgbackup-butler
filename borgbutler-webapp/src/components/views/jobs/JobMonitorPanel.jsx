import React from 'react';
import {Button} from 'reactstrap';
import {getRestServiceUrl, isDevelopmentMode} from '../../../utilities/global';
import JobQueue from './JobQueue';
import ErrorAlert from '../archives/ArchiveView';
import PropTypes from 'prop-types';

class JobMonitorPanel extends React.Component {
    state = {
        isFetching: false,
        testMode: false
    };

    componentDidMount = () => {
        this.fetchArchive();
        this.interval = setInterval(() => this.fetchArchive(), 2000);
    };

    componentWillUnmount() {
        clearInterval(this.interval);
    }

    toggleTestMode() {
        this.setState({
            testMode: !this.state.testMode
        });
    }

    fetchArchive = () => {
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('jobs', {
            repo: this.props.repo,
            testMode: this.state.testMode
        }), {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(response => response.json())
            .then(json => {
                const queues = json.map(queue => {
                    return queue;
                });
                this.setState({
                    isFetching: false,
                    queues
                });
            })
            .catch(() => this.setState({isFetching: false, failed: true}));
    };

    render() {
        let content = '';
        let todo = '';

        if (this.state.isFetching && !this.state.queues) {
            content = <i>Loading...</i>;
        } else if (this.state.failed) {
            content = <ErrorAlert
                title={'Cannot load Repositories'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchArchive,
                    title: 'Try again'
                }}
            />;
        } else if (this.state.queues) {
            if (this.state.queues.length > 0) {
                content = <React.Fragment>
                    {this.state.queues
                        .map((queue) => <JobQueue
                            embedded={this.props.embedded}
                            queue={queue}
                            key={queue.repo}
                        />)}
                </React.Fragment>;
            } else if (isDevelopmentMode() && !this.props.embedded) {
                content = <React.Fragment>No jobs are running or queued.<br/><br/>
                    <Button color="primary" onClick={this.toggleTestMode}>Test mode</Button>
                </React.Fragment>
            } else {
                content = <React.Fragment>No jobs are running or queued.</React.Fragment>
            }
        }
        return <React.Fragment>
            {content}
        </React.Fragment>;
    }

    constructor(props) {
        super(props);

        this.fetchArchive = this.fetchArchive.bind(this);
        this.toggleTestMode = this.toggleTestMode.bind(this);
    }
}

JobMonitorPanel.propTypes = {
    embedded: PropTypes.bool,
    repo: PropTypes.string
};

JobMonitorPanel.defaultProps = {
    embedded: true,
    repo: null
};


export default JobMonitorPanel;
