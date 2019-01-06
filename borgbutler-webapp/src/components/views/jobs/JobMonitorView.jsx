import React from 'react'
import {PageHeader} from '../../general/BootstrapComponents';
import JobMonitorPanel from "./JobMonitorPanel";

class JobMonitorView extends React.Component {

    render = () => {
        return <React.Fragment>
            <PageHeader>
                Job monitor
            </PageHeader>
            <JobMonitorPanel embedded={false}/>
        </React.Fragment>;
    };
}

export default JobMonitorView;
