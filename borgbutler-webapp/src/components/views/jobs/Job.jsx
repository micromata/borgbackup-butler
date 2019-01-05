import React from 'react';
import {Button, Card, CardBody, Collapse, Progress} from 'reactstrap';
import {IconCancelJob} from '../../general/IconComponents'

class Job extends React.Component {
    constructor(props) {
        super(props);
        this.toggle = this.toggle.bind(this);
        this.state = {collapse: false};
    }

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
        return (
            <div>
                <Button color="link" onClick={this.toggle}>{job.description}</Button>
                <div>{content}<IconCancelJob/></div>
                <Collapse isOpen={this.state.collapse}>
                    <Card>
                        <CardBody>
                            <table><tbody><tr><th>Status</th><td>{job.status}</td></tr>
                            <tr><th>Command line</th><td>{job.commandLineAsString}</td></tr></tbody></table>
                        </CardBody>
                    </Card>
                </Collapse>
            </div>
        )
    }
}

export default Job;
