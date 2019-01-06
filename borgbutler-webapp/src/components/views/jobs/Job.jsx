import React from 'react';
import {Redirect} from 'react-router-dom';
import {Button, Card, CardBody, Collapse, ListGroupItem, Progress} from 'reactstrap';
import {IconCancel} from '../../general/IconComponents'
import {getRestServiceUrl} from "../../../utilities/global";
import PropTypes from "prop-types";
import './Job.css';

class Job extends React.Component {
    constructor(props) {
        super(props);
        this.toggle = this.toggle.bind(this);
        this.cacnelJob = this.cancelJob.bind(this);
        this.state = {
            collapse: false,
            redirect: false
        };
    }

    cancelJob = (jobId) => {
        fetch(getRestServiceUrl('jobs/cancel', {
            uniqueJobNumber: jobId
        }));
        this.setState({
            redirect: true
        })
    };

    toggle() {
        this.setState({collapse: !this.state.collapse});
    }

    renderRedirect = () => {
        if (this.props.embedded && this.state.redirect) {
            return <Redirect to='/jobmonitor'/>
        }
    }

    render() {
        let content = undefined;
        let job = this.props.job;
        if (job.status === 'RUNNING') {
            let progressPercent = 100;
            let color = 'success';
            if (job.cancellationRequested) {
                color = 'warning';
            }
            if (job.progressPercent >= 0 && job.progressPercent <= 100) {
                progressPercent = job.progressPercent;
            }
            content = <Progress animated color={color} value={progressPercent}>{job.progressText}</Progress>;
        } else {
            content = <Progress color={'info'} value={100}>{job.status}</Progress>
        }
        let cancelDisabled = undefined;
        if ((job.status !== 'RUNNING' && job.status !== 'QUEUED') || job.cancellationRequested) {
            cancelDisabled = true;
        }
        return (
            <ListGroupItem>
                {this.renderRedirect()}
                <div className="row">
                    <div className="job-progress">
                        <Button color="link" onClick={this.toggle}>{job.description}</Button>
                        {content}
                    </div>
                    <div className="job-cancel"><Button color={'danger'} onClick={() => this.cancelJob(job.uniqueJobNumber)}
                                             disabled={cancelDisabled}><IconCancel/></Button>
                    </div>
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
            </ListGroupItem>
        )
    }
}

Job.propTypes = {
    embedded: PropTypes.bool
};

export default Job;
