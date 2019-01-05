import React from 'react';
import {Progress} from 'reactstrap';

function Job({job}) {
    let content = undefined;
    if (job.status === 'RUNNING') {
        content = <Progress animated color={'success'} value="100">{job.progressText}</Progress>;
    } else {
        content =  <Progress color={'info'} value="100">{job.status}</Progress>
    }
    return (
        <div>
            <div>{job.description}</div>
            {content}
        </div>
    )
}

export default Job;
