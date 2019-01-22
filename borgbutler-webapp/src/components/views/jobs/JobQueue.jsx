import React from 'react';
import {Card, CardBody, CardHeader, Collapse, ListGroup} from 'reactstrap';
import Job from "./Job";

class JobQueue extends React.Component {
    state = {
        collapsed: true
    };

    toggle() {
        this.setState({collapsed: !this.state.collapsed});
    }


    render() {
        const queue = this.props.queue;
        return (
            <div>
                <Card>
                    <CardHeader className={'onclick'} onClick={this.toggle}>Job queue: {queue.repo}</CardHeader>
                    <Collapse isOpen={this.state.collapsed}>
                        <CardBody>
                            <ListGroup>
                                {queue.jobs
                                    .map((job, index) => <Job
                                        embedded={this.props.embedded}
                                        job={job}
                                        key={job.uniqueJobNumber}
                                    />)}
                            </ListGroup>
                        </CardBody>
                    </Collapse>
                </Card>
            </div>
        );
    }

    constructor(props) {
        super(props);
        this.toggle = this.toggle.bind(this);

    }
}

export default JobQueue;
