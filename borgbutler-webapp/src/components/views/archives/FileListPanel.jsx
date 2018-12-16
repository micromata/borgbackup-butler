import React from 'react'
import {getRestServiceUrl} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import FileListTable from "./FileListTable";

class ArchiveView extends React.Component {

    state = {
        isFetching: false, activeTab: '1',
        fileList : undefined
    };

    componentDidMount = () => {
        this.fetchArchiveFileList(false);
    };


    fetchArchiveFileList = (force) => {
        let forceReload = false;
        if (force && window.confirm('Are you sure you want to reload the archive file list? This may take a long time...')) {
            forceReload = true;
        }
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('archives/filelist', {
            archiveId: this.props.archiveId,
            force: forceReload
        }), {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(response => response.json())
            .then(json => {
                this.setState({
                    isFetching: false,
                    fileList: json
                })
            })
            .catch(() => this.setState({isFetching: false, failed: true}));
    };

    render = () => {
        let content = undefined;

        if (this.state.isFetching) {
            content = <i>Loading...</i>;
        } else if (this.state.failed) {
            content = <ErrorAlert
                title={'Cannot load Archive file list'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchArchive,
                    title: 'Try again'
                }}
            />;
        } else if (this.state.fileList) {
            content = <React.Fragment>
                <FileListTable
                entries={this.state.fileList}/>
            </React.Fragment>;
        }
        return <React.Fragment>
            {content}
        </React.Fragment>;
    };

    constructor(props) {
        super(props);

        this.fetchArchiveFileList = this.fetchArchiveFileList.bind(this);
    }
}

export default ArchiveView;
