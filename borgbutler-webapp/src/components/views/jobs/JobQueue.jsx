import React from 'react';
import {Button, Card, CardBody, Collapse} from 'reactstrap';
import Job from "./Job";
import PropTypes from "prop-types";

class JobQueue extends React.Component {
    constructor(props) {
        super(props);
        this.toggle = this.toggle.bind(this);
        this.state = {collapse: true};
    }

    toggle() {
        this.setState({collapse: !this.state.collapse});
    }

    render() {
        return (
            <div>
                <Button color="primary" onClick={this.toggle} style={{ marginBottom: '1rem' }}>{this.props.queue.repo}</Button>
                <Collapse isOpen={this.state.collapse}>
                    <Card>
                        <CardBody>
                            {this.props.queue.jobs
                                .map((job, index) => <Job
                                    embedded={this.props.embedded}
                                    job={job}
                                    key={job.commandLineAsString}
                                />)}
                        </CardBody>
                    </Card>
                </Collapse>
            </div>
        );
    }
}

JobQueue.propTypes = {
    embedded: PropTypes.bool
};

export default JobQueue;
