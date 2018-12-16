import React from 'react'
import {getRestServiceUrl, humanFileSize} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';

class ArchiveView extends React.Component {

    state = {
        isFetching: false, activeTab: '1',
    };

    componentDidMount = () => {
        this.fetchArchiveFileList();
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
            archive: this.props.archiveId,
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
                    archive: json
                })
            })
            .catch(() => this.setState({isFetching: false, failed: true}));
    };

    render = () => {
        let content = undefined;
        let archive = this.state.archive;

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
        } else if (this.state.archive) {
            content = <React.Fragment>
                Hurzel;
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
