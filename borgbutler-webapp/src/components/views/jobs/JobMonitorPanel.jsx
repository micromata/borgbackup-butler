import React from 'react';
import {Button} from 'reactstrap';
import {getRestServiceUrl, isDevelopmentMode} from "../../../utilities/global";
import JobQueue from "./JobQueue";
import ErrorAlert from "../archives/ArchiveView";

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
            if (this.state.queues.length > 0) {
                content = <React.Fragment>
                    {this.state.queues
                        .map((queue) => <JobQueue
                            queue={queue}
                            key={queue.repo}
                        />)}
                </React.Fragment>;
            } else if (isDevelopmentMode()) {
                content = <React.Fragment>No jobs are running or queued.<br/><br/>
                    <Button color="primary" onClick={this.toggleTestMode}>Test mode</Button>
                </React.Fragment>
            } else {
                content = <React.Fragment>No jobs are running or queued.</React.Fragment>
            }
        }
        if (isDevelopmentMode()) {
            todo = <React.Fragment><br/>
                <code>
                    <h2>To-do</h2>
                    Zum Testen den Button "Test-Mode" drücken.
                    <ol>
                        <li>Kein Geflackere: Nach einem fetch (alle 2 Sekunden) wird der das ganze DOM neu gerendert, obwohl sich eigentlich nix
                        geändert hat. Schön wäre, wenn sich nur die Komponenten neu rendern, die auch in den REST-Daten von fetch sich geändert haben.</li>
                        <li>Toggle-Status z. B. von My Computer merken (zum Reproduzieren: Klick auf "My Computer"..</li>
                        <li>Cancel-Button soll rechts neben die Progressbar.</li>
                    </ol>
                </code></React.Fragment>
        }
        return <React.Fragment>
            {content}
            {todo}
        </React.Fragment>;
    }

    constructor(props) {
        super(props);

        this.fetchArchive = this.fetchArchive.bind(this);
        this.toggleTestMode = this.toggleTestMode.bind(this);
    }
}

export default JobMonitorPanel;