import React from 'react';
import Job from "./Job";

function JobQueue({queue}) {
    return (
        <div>
            <h2>{queue.repo}</h2>
            {queue.jobs
                .map((job, index) => <Job
                    job={job}
                    key={job.commandLineAsString}
                />)}
        </div>
    )
}
export default JobQueue;
