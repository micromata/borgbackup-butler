import React from 'react';
import {Redirect} from 'react-router-dom';
import {Button, Card, CardBody, Collapse, ListGroupItem, Progress, Table} from 'reactstrap';
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
        let cancelDisabled = undefined;
        if ((job.status !== 'RUNNING' && job.status !== 'QUEUED') || job.cancellationRequested) {
            cancelDisabled = true;
        }
        let cancelButton = <div className="job-cancel"><Button color={'danger'}
                                                               onClick={() => this.cancelJob(job.uniqueJobNumber)}
                                                               disabled={cancelDisabled}><IconCancel/></Button>
        </div>;
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
        } else if (job.status === 'CANCELLED') {
            content = <Progress color={'warning'} value={100}>{job.status}</Progress>
            cancelButton = '';
        } else if (job.status === 'DONE') {
            content = <Progress color={'success'} value={100}>{job.status}</Progress>
            cancelButton = '';
        } else {
            content = <Progress color={'info'} value={100}>{job.status}</Progress>
        }
        let environmentVariables = '';
        if (job.environmentVariables && Array.isArray(job.environmentVariables)) {
            environmentVariables = job.environmentVariables.map((variable, index) => <React.Fragment key={index}>
                export &quot;{variable}&quot;;<br/>
            </React.Fragment>)
        }
        return (
            <ListGroupItem>
                {this.renderRedirect()}
                <div className="row">
                    <div className="job-progress">
                        <Button color="link" onClick={this.toggle}>{job.description}</Button>
                        {content}
                    </div>
                    {cancelButton}
                </div>
                <Collapse isOpen={this.state.collapse}>
                    <Card>
                        <CardBody>
                            <Table striped bordered hover>
                                <tbody>
                                <tr>
                                    <th>Status</th>
                                    <td>{job.status}</td>
                                </tr>
                                <tr>
                                    <th>Command line</th>
                                    <td>{job.commandLineAsString}</td>
                                </tr>
                                <tr>
                                    <th>Environment</th>
                                    <td>{environmentVariables}</td>
                                </tr>
                                </tbody>
                            </Table>
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
