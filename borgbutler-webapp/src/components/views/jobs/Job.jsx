import React from 'react';
import {Button, Card, CardBody, Collapse, Progress} from 'reactstrap';
import {IconCancel} from '../../general/IconComponents'
import {getRestServiceUrl} from "../../../utilities/global";

class Job extends React.Component {
    constructor(props) {
        super(props);
        this.toggle = this.toggle.bind(this);
        this.cacnelJob = this.cancelJob.bind(this);
        this.state = {collapse: false};
    }

    cancelJob = (jobId) => {
        fetch(getRestServiceUrl('jobs/cancel', {
            uniqueJobNumber: jobId
        }));
    };

    toggle() {
        this.setState({collapse: !this.state.collapse});
    }

    render() {
        let content = undefined;
        let job = this.props.job;
        if (job.status === 'RUNNING') {
            let progressPercent = 100;
            if (job.progressPercent >= 0 && job.progressPercent <= 100) {
                progressPercent = job.progressPercent;
            }
            content = <Progress animated color={'success'} value={progressPercent}>{job.progressText}</Progress>;
        } else {
            content = <Progress color={'info'} value={100}>{job.status}</Progress>
        }
        let cancelDisabled = undefined;
        if (job.status !== 'RUNNING' && job.status !== 'QUEUED') {
            cancelDisabled = true;
        }
        return (
            <div>
                <Button color="link" onClick={this.toggle}>{job.description}</Button>
                <div>{content}
                    <Button color={'danger'} onClick={() => this.cancelJob(job.uniqueJobNumber)} disabled={cancelDisabled}><IconCancel/></Button>
                </div>
                <Collapse isOpen={this.state.collapse}>
                    <Card>
                        <CardBody>
                            <table>
                                <tbody>
                                <tr>
                                    <th>Status</th>
                                    <td>{job.status}</td>
                                </tr>
                                <tr>
                                    <th>Command line</th>
                                    <td>{job.commandLineAsString}</td>
                                </tr>
                                </tbody>
                            </table>
                        </CardBody>
                    </Card>
                </Collapse>
            </div>
        )
    }
}

export default Job;
