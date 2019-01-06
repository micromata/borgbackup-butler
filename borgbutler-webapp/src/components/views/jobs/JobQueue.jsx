import React from 'react';
import {Card, CardBody, CardHeader, ListGroup} from 'reactstrap';
import Job from "./Job";

function JobQueue({queue, embedded}) {
    return (
        <div>
            <Card>
                <CardHeader>{queue.repo}</CardHeader>
                <CardBody>
                    <ListGroup>
                        {queue.jobs
                            .map((job, index) => <Job
                                embedded={embedded}
                                job={job}
                                key={job.uniqueJobNumber}
                            />)}
                    </ListGroup>
                </CardBody>
            </Card>
        </div>
    );
}

export default JobQueue;
