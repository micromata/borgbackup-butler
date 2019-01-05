import React from 'react';
import {Progress} from 'reactstrap';
import {IconCancelJob} from '../../general/IconComponents'

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
            <div>{content}<IconCancelJob /></div>
        </div>
    )
}

export default Job;
