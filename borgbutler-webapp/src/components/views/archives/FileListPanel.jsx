import React from 'react'
import {Button} from 'reactstrap';
import {getRestServiceUrl} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import FileListTable from "./FileListTable";
import FileListFilter from "./FileListFilter";

class ArchiveView extends React.Component {

    state = {
        isFetching: false, activeTab: '1',
        fileList: undefined,
        filter: {
            search: '',
            maxSize: '50'
        }
    };

    componentDidMount = () => {
        this.fetchArchiveFileList(false);
    };

    handleInputChange = (event) => {
        event.preventDefault();
        this.setState({filter : {...this.state.filter, [event.target.name]: event.target.value}});
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
            force: forceReload,
            searchString: this.state.filter.search,
            maxResultSize: this.state.filter.maxSize
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
            if (this.state.fileList.length > 0) {
                content = <React.Fragment>
                    <FileListFilter
                        filter={this.state.filter}
                        changeFilter={this.handleInputChange}
                        reload={(event) => {
                            event.preventDefault();
                            this.fetchArchiveFileList();
                        }}
                    />
                    <FileListTable
                        entries={this.state.fileList}/>
                </React.Fragment>;
            } else {
                content = <React.Fragment>
                    <Button outline color="primary" onClick={() => this.fetchArchiveFileList(true)}>Load file list from
                        borg backup server</Button>
                </React.Fragment>;
            }
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
