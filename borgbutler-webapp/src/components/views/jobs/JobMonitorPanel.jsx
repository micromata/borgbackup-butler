import React from 'react';
import {Button, Collapse} from 'reactstrap';
import {getRestServiceUrl, isDevelopmentMode} from '../../../utilities/global';
import JobQueue from './JobQueue';
import ErrorAlert from '../../general/ErrorAlert';
import PropTypes from 'prop-types';

class JobMonitorPanel extends React.Component {
    state = {
        isFetching: false,
        isFetchingOldJobs: false,
        testMode: false,
        collapseOldJobs: false
    };

    componentDidMount = () => {
        this.fetchQueues(false);
        this.interval = setInterval(() => this.fetchJobs(), 2000);
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

    fetchJobs() {
        if (!this.state.isFetching) { // Don't run twice at the same time
            this.fetchQueues(false);
        }
        if (this.state.collapseOldJobs && !this.state.isFetchingOldJobs) {
            this.fetchQueues(true);
        }
    }

    fetchQueues = (oldJobs) => {
        let queuesVar = 'queues';
        let isFetchingVar = 'isFetching';
        let failedVar = 'failed';
        if (oldJobs) {
            queuesVar = 'oldJobsQueues';
            isFetchingVar = 'isFetchingOldJobs';
            failedVar = 'oldJobsFailed';
        }
        this.setState({
            [isFetchingVar]: true,
            [failedVar]: false
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
                this.setState({
                    [isFetchingVar]: false,
                    [queuesVar]: queues
                });
            })
            .catch(() => this.setState({[isFetchingVar]: false, [failedVar]: true}));
    };

    render() {
        let content = '';

        if (this.state.isFetching && !this.state.queues) {
            content = <i>Loading...</i>;
        } else if (this.state.failed) {
            content = <ErrorAlert
                title={'Cannot load job queues'}
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
            } else {
                content = <React.Fragment>No jobs are running or queued.</React.Fragment>
            }
        }
        let oldJobs = '';

        if (this.state.isFetchingOldJobs && !this.state.oldJobsQueues) {
            oldJobs = <i>Loading...</i>;
        } else if (this.state.oldJobsFailed) {
            oldJobs = <ErrorAlert
                title={'Cannot load old job queues'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchQueues,
                    title: 'Try again'
                }}
            />;
        } else if (this.state.oldJobsQueues) {
            if (this.state.oldJobsQueues.length > 0) {
                oldJobs = <React.Fragment>
                    {this.state.oldJobsQueues
                        .map((queue) => <JobQueue
                            embedded={this.props.embedded}
                            queue={queue}
                            key={queue.repo}
                        />)}
                </React.Fragment>
            } else {
                oldJobs = <React.Fragment>No old jobs available.</React.Fragment>
            }
        }
        let testContent = '';
        if (isDevelopmentMode() && !this.props.embedded) {
            testContent = <React.Fragment><br/><br/><br/><Button className="btn-outline-info" onClick={this.toggleTestMode}>Test mode</Button></React.Fragment>;
        }

        return <React.Fragment>
            {content}
            <h5 className={'onclick'} onClick={this.toggleOldJobs}>Show old jobs
            </h5>
            <Collapse isOpen={this.state.collapseOldJobs}>
                {oldJobs}
            </Collapse>
            {testContent}
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
