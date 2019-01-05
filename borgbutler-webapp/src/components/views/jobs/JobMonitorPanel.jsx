import React from 'react';
import {getRestServiceUrl} from "../../../utilities/global";
import JobQueue from "./JobQueue";
import ErrorAlert from "../archives/ArchiveView";

class JobMonitorPanel extends React.Component {
    state = {
        isFetching: false
    };

    componentDidMount = () => {
        this.fetchArchive();
    };


    fetchArchive = (force) => {
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('jobs'), {
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
                })
            })
            .catch(() => this.setState({isFetching: false, failed: true}));
    };

    render() {
        let content = undefined;

        if (this.state.isFetching) {
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
            content = <React.Fragment>
                {this.state.queues
                    .map((queue) => <JobQueue
                        queue={queue}
                        key={queue.repo}
                    />)}
            </React.Fragment>;

        }
        return <React.Fragment>
            {content}
        </React.Fragment>;
    }

    constructor(props) {
        super(props);

        this.fetchArchive = this.fetchArchive.bind(this);
    }
}

export default JobMonitorPanel;