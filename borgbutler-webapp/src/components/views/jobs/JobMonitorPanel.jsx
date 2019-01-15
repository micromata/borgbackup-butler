import React from 'react';
import {Button, Collapse} from 'reactstrap';
import {getRestServiceUrl, isDevelopmentMode} from '../../../utilities/global';
import JobQueue from './JobQueue';
import ErrorAlert from '../archives/ArchiveView';
import PropTypes from 'prop-types';

class JobMonitorPanel extends React.Component {
    state = {
        isFetching: false,
        testMode: false,
        collapseOldJobs: false
    };

    componentDidMount = () => {
        this.fetchQueues(false);
        this.interval = setInterval(() => this.fetchQueues(false), 2000);
    };

    componentWillUnmount() {
        clearInterval(this.interval);
    }

    toggleTestMode() {
        this.setState({
            testMode: !this.state.testMode
        });
    }

    toggleOldJobs() {
        if (!this.state.collapseOldJobs) {
            this.fetchQueues(true);
        }
        this.setState({collapseOldJobs: !this.state.collapseOldJobs});
    }


    fetchQueues = (oldJobs) => {
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('jobs', {
            repo: this.props.repo,
            testMode: this.state.testMode,
            oldJobs: oldJobs
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
                if (oldJobs) {
                    this.setState({
                        isFetching: false,
                        oldJobsQueues: queues
                    });
                } else {
                    this.setState({
                        isFetching: false,
                        queues: queues
                    });
                }
            })
            .catch(() => this.setState({isFetching: false, failed: true}));
    };

    render() {
        let content = '';

        if (this.state.isFetching && !this.state.queues) {
            content = <i>Loading...</i>;
        } else if (this.state.failed) {
            content = <ErrorAlert
                title={'Cannot load Repositories'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchQueues,
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
        let oldJobs = 'Old jobs...';
        if (this.state.oldJobsQueues && this.state.oldJobsQueues.length > 0) {
            oldJobs = <React.Fragment>
                {this.state.oldJobsQueues
                    .map((queue) => <JobQueue
                        embedded={this.props.embedded}
                        queue={queue}
                        key={queue.repo}
                    />)}
            </React.Fragment>
        }
        return <React.Fragment>
            {content}
            <h5 onClick={this.toggleOldJobs}>Show old jobs
            </h5>
            <Collapse isOpen={this.state.collapseOldJobs}>
                {oldJobs}
            </Collapse>
        </React.Fragment>;
    }

    constructor(props) {
        super(props);

        this.fetchQueues = this.fetchQueues.bind(this);
        this.toggleTestMode = this.toggleTestMode.bind(this);
        this.toggleOldJobs = this.toggleOldJobs.bind(this);
    }
}

JobMonitorPanel
    .propTypes = {
    embedded: PropTypes.bool,
    repo: PropTypes.string
};

JobMonitorPanel
    .defaultProps = {
    embedded: true,
    repo: null
};


export default JobMonitorPanel;
