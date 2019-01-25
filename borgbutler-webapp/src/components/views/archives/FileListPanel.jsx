import React from 'react'
import cookie from 'react-cookies';
import {withRouter} from 'react-router-dom';
import {Breadcrumb, Button} from 'reactstrap';
import {getRestServiceUrl} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import FileListTable from './FileListTable';
import FileListFilter from './FileListFilter';
import JobMonitorPanel from '../jobs/JobMonitorPanel';
import BreadcrumbPath from './BreadcrumbPath';

class FileListPanel extends React.Component {

    constructor(props) {
        super(props);

        this.fetchArchiveFileList = this.fetchArchiveFileList.bind(this);
        this.handleURLChange = this.handleURLChange.bind(this);
        this.componentWillMount = this.componentWillMount.bind(this);

        this.unregisterHistoryListener = props.history.listen(this.handleURLChange);
        this.state = {
            isFetching: false, activeTab: '1',
            fileList: undefined,
            filter: {
                search: '',
                mode: 'tree',
                currentDirectory: '',
                maxSize: '50',
                diffArchiveId: '',
                autoChangeDirectoryToLeafItem: true,
                openDownloads: true
            },
            forceStepIntoDir: false
        };
        // Resetting the NoReFetch State
        if (props.location.state) {
            props.location.state.noReFetch = undefined;
        }
    }

    componentWillMount = () => {
        ['mode', 'maxSize', 'autoChangeDirectoryToLeafItem', 'openDownloads'].forEach(function (variable) {
            const value = cookie.load(`file-list-${variable}`);
            if (value) {
                //console.log('Restoring ' + variable + '=' + value);
                //this.setState({filter: {...this.state.filter, variable: value}});
            }
        });
    }

    componentDidMount = () => {
        this.handleURLChange(this.props.location);
    };

    componentWillUnmount() {
        this.unregisterHistoryListener();
    }

    handleURLChange = (location, action) => {

        // When Action is going back and the last step was a force step go one extra
        if (action === 'POP' && this.state.forceStepIntoDir) {
            this.props.history.go(-1);
            this.setState({forceStepIntoDir: false});
            return;
        }

        const noReFetch = location.state && location.state.noReFetch;

        this.setState({forceStepIntoDir: noReFetch});

        if (noReFetch) {
            return;
        }

        // Replacing matched URL(archiveId, repoId) and removing trailing slashes
        this.changeCurrentDirectory(location.pathname.replace(this.props.match.url, '').replace(/^\/|\/$/g, ''));
    };

    handleInputChange = (event, callback) => {
        event.preventDefault();
        const variable = event.target.name;
        this.setState({filter: {...this.state.filter, [event.target.name]: event.target.value}},
            () => {
                if (variable === 'mode') {
                    this.fetchArchiveFileList();
                }
                if (callback) {
                    callback();
                }
            });
        if (['mode', 'maxSize'].indexOf(variable) >= 0) {
            //console.log('Saving ' + variable + '=' + event.target.value);
            cookie.save(`file-list-${variable}`, event.target.value, {path: "/"});
        }
    };

    handleCheckboxChange = event => {
        const variable = event.target.name;
        this.setState({filter: {...this.state.filter, [variable]: event.target.checked}});
        if (['autoChangeDirectoryToLeafItem', 'openDownloads'].indexOf(variable)) {
            //console.log('Saving ' + variable + '=' + event.target.checked);
            cookie.save(`file-list-${variable}`, event.target.checked, {path: "/"});
        }
    }

    changeCurrentDirectory = (currentDirectory) => {
        this.setState({filter: {...this.state.filter, currentDirectory: currentDirectory}},
            () => {
                this.fetchArchiveFileList();
            });
    };

    fetchArchiveFileList = (force) => {
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('archives/filelist', {
            archiveId: this.props.archive.id,
            diffArchiveId: this.state.filter.diffArchiveId,
            force: force,
            searchString: this.state.filter.search,
            mode: this.state.filter.mode,
            currentDirectory: this.state.filter.currentDirectory,
            maxResultSize: this.state.filter.maxSize,
            diffArchive: this.state.filter.diffArchive,
            autoChangeDirectoryToLeafItem: this.state.filter.autoChangeDirectoryToLeafItem
        }), {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(response => response.json())
            .then(json => {
                let currentDirectory = this.state.filter.currentDirectory;
                const fileList = json;
                if (fileList && fileList.length > 0 && fileList[0] && fileList[0].path) {
                    const path = fileList[0].path;
                    currentDirectory = path.substring(0, path.length - fileList[0].displayPath.length);
                    // console.log(fileList[0].path + ", displayPath=" + fileList[0].displayPath + " -> " + currentDirectory);

                    let newUrl = this.props.match.url;

                    if (currentDirectory) {
                        // Avoid double leading slash occurring if top directory of backup has only one single sub directory the server steps automatically in:
                        newUrl += newUrl.endsWith('/') ? currentDirectory : `/${currentDirectory}`;
                    }

                    if (newUrl !== this.props.location.pathname) {
                        this.props.history.push(newUrl, {noReFetch: true});
                    }
                }
                this.setState({
                    isFetching: false,
                    fileList: fileList,
                    filter: {...this.state.filter, currentDirectory: currentDirectory}
                })
            })
            .catch(() => this.setState({isFetching: false, failed: true}));
    };

    render = () => {
        let content = undefined;

        if (this.state.isFetching) {
            content = <JobMonitorPanel repo={this.props.repoId}/>;
        } else if (this.state.failed) {
            content = <ErrorAlert
                title={'Cannot load Archive file list'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchQueues,
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
                let breadcrumb;
                if (this.state.filter.mode === 'tree' && this.state.filter.currentDirectory.length > 0) {
                    breadcrumb = (
                        <Breadcrumb>
                            <BreadcrumbPath match={this.props.match}/>
                        </Breadcrumb>
                    );
                }

                content = <React.Fragment>
                    <FileListFilter
                        filter={this.state.filter}
                        changeFilter={this.handleInputChange}
                        changeFilterCheckbox={this.handleCheckboxChange}
                        reload={(event) => {
                            event.preventDefault();
                            this.fetchArchiveFileList();
                        }}
                        currentArchiveId={this.props.archive.id}
                        archiveShortInfoList={this.props.archiveShortInfoList}
                    />
                    {breadcrumb}
                    <FileListTable
                        archive={this.props.archive}
                        diffArchiveId={this.state.filter.diffArchiveId}
                        openDownloads={this.state.filter.openDownloads}
                        entries={this.state.fileList}
                        search={this.state.filter.search}
                        mode={this.state.filter.mode}
                    />
                </React.Fragment>;
            }
        }
        return <React.Fragment>
            {content}
        </React.Fragment>;
    };
}

export default withRouter(FileListPanel);
