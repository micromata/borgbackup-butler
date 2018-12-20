import React from 'react'
import {Button, Breadcrumb, BreadcrumbItem} from 'reactstrap';
import {getRestServiceUrl} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import FileListTable from "./FileListTable";
import FileListFilter from "./FileListFilter";

class FileListPanel extends React.Component {

    state = {
        isFetching: false, activeTab: '1',
        fileList: undefined,
        filter: {
            search: '',
            mode: 'tree',
            currentDirectory: '',
            maxSize: '50',
            diffArchiveId: ''
        }
    };

    componentDidMount = () => {
        this.fetchArchiveFileList(false);
    };

    handleInputChange = (event) => {
        event.preventDefault();
        let target = event.target.name;
        this.setState({filter: {...this.state.filter, [event.target.name]: event.target.value}},
            () => {
                if (target === 'mode') {
                    this.fetchArchiveFileList();
                }
            });
    };

    changeCurrentDirectory = (currentDirectory) => {
        this.setState({filter: {...this.state.filter, currentDirectory: currentDirectory}},
            () => {
                this.fetchArchiveFileList();
            });
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
            diffArchiveId: this.state.filter.diffArchiveId,
            force: forceReload,
            searchString: this.state.filter.search,
            mode: this.state.filter.mode,
            currentDirectory: this.state.filter.currentDirectory,
            maxResultSize: this.state.filter.maxSize,
            diffArchive: this.state.filter.diffArchive
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
        let breadcrumb = undefined;

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
            if (this.state.fileList.length === 1 && this.state.fileList[0].mode === 'notLoaded') {
                content = <React.Fragment>
                    <Button outline color="primary" onClick={() => this.fetchArchiveFileList(true)}>Load file list from
                        borg backup server</Button>
                </React.Fragment>;
            } else {
                if (this.state.filter.mode === 'tree' && this.state.filter.currentDirectory.length > 0) {
                    let dirs = this.state.filter.currentDirectory.split('/');
                    let breadcrumbs = [];
                    for (let i = 0; i < dirs.length - 1; i++) {
                        let path = '';
                        for (let j = 0; j <= i; j++) {
                            path += dirs[j];
                            if (j < i) {
                                path += '/';
                            }
                        }
                        breadcrumbs.push(<BreadcrumbItem key={i}><Button color={'link'} onClick={() => this.changeCurrentDirectory(path)}
                                                                  >{dirs[i]}</Button></BreadcrumbItem>);
                    }
                    breadcrumb = <Breadcrumb>
                        <BreadcrumbItem><Button color={'link'} onClick={() => this.changeCurrentDirectory('')}
                                                       >Top</Button></BreadcrumbItem>
                        {breadcrumbs}
                        <BreadcrumbItem active>{dirs[dirs.length - 1]}</BreadcrumbItem>
                    </Breadcrumb>;
                } else {
                    breadcrumb = '';
                }
                content = <React.Fragment>
                    <FileListFilter
                        filter={this.state.filter}
                        changeFilter={this.handleInputChange}
                        reload={(event) => {
                            event.preventDefault();
                            this.fetchArchiveFileList();
                        }}
                        currentArchiveId={this.props.archiveId}
                        archiveShortInfoList={this.props.archiveShortInfoList}
                    />
                    {breadcrumb}
                    <FileListTable
                        archiveId={this.props.archiveId}
                        diffArchiveId={this.state.filter.diffArchiveId}
                        entries={this.state.fileList}
                        search={this.state.filter.search}
                        mode={this.state.filter.mode}
                        changeCurrentDirectory={this.changeCurrentDirectory}/>
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

export default FileListPanel;
