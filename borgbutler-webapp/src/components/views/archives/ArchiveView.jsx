import React from 'react'
import {Nav, NavLink, TabContent, Table, TabPane} from 'reactstrap';
import {PageHeader} from '../../general/BootstrapComponents';
import {getRestServiceUrl, humanFileSize, humanSeconds} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import {IconRefresh} from '../../general/IconComponents';
import classNames from 'classnames';
import FileListPanel from './FileListPanel';
import JobMonitorPanel from '../jobs/JobMonitorPanel';
import {Link} from "react-router-dom";
import ConfirmModal from '../../general/modal/ConfirmModal';

class ArchiveView extends React.Component {

    componentDidMount = () => {
        this.fetchArchive();
    };


    fetchArchive = (force) => {
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('archives', {
            repo: this.state.repoId,
            archiveId: this.state.archiveId,
            force: force
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

    toggleTab = tab => () => {
        this.setState({
            activeTab: tab
        })
    };

    toggleModal() {
        this.setState({
            confirmModal: !this.state.confirmModal
        })
    }

    render = () => {
        let content = undefined;
        let archive = this.state.archive;
        let pageHeader = '';

        if (this.state.isFetching) {
            content = <JobMonitorPanel repo={this.state.repoId}/>;
        } else if (this.state.failed) {
            content = <ErrorAlert
                title={'Cannot load Repositories'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchArchive,
                    title: 'Try again'
                }}
            />;
        } else if (this.state.archive) {
            pageHeader = <React.Fragment>
                <Link to={'/repos'}> Repositories</Link> -
                <Link
                    to={`/repoArchives/${this.state.repoId}/${archive.repoDisplayName}`}> {archive.repoDisplayName}</Link> - {archive.name}
                <div
                    className={'btn btn-outline-primary refresh-button-right'}
                    onClick={this.toggleModal}
                >
                    <IconRefresh/>
                </div>
            </React.Fragment>;
            let stats = null;
            if (archive.stats) {
                stats = <tr>
                    <td>Stats</td>
                    <td>
                        <table className="inline">
                            <tbody>
                            <tr>
                                <td>Compressed size</td>
                                <td>{humanFileSize(archive.stats.compressed_size)}</td>
                            </tr>
                            <tr>
                                <td>Deduplicated size</td>
                                <td>{humanFileSize(archive.stats.deduplicated_size)}</td>
                            </tr>
                            <tr>
                                <td>Original size</td>
                                <td>{humanFileSize(archive.stats.original_size)}</td>
                            </tr>
                            <tr>
                                <td>Number of files</td>
                                <td>{Number(archive.stats.nfiles).toLocaleString()}</td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
            }
            content = <React.Fragment>
                <Nav tabs>
                    <NavLink
                        className={classNames({active: this.state.activeTab === '1'})}
                        onClick={this.toggleTab('1')}
                    >
                        File list
                    </NavLink>
                    <NavLink
                        className={classNames({active: this.state.activeTab === '2'})}
                        onClick={this.toggleTab('2')}
                    >
                        Information
                    </NavLink>
                </Nav>
                <TabContent activeTab={this.state.activeTab}>
                    <TabPane tabId={'1'}>
                        <FileListPanel
                            repoId={this.state.repoId}
                            archive={archive}
                            archiveShortInfoList={archive.archiveShortInfoList}
                        />
                    </TabPane>
                    <TabPane tabId={'2'}>
                        <Table striped bordered hover>
                            <tbody>
                            <tr>
                                <td>Archive</td>
                                <td>{archive.name}</td>
                            </tr>
                            <tr>
                                <td>Start - end</td>
                                <td>{archive.start} - {archive.end} (Duration: {humanSeconds(archive.duration)})</td>
                            </tr>
                            <tr>
                                <td>Id</td>
                                <td>{archive.id}</td>
                            </tr>
                            {stats}
                            <tr>
                                <td>Comment</td>
                                <td>{archive.comment}</td>
                            </tr>
                            <tr>
                                <td>Command line</td>
                                <td>{archive.commandLine ? archive.commandLine.join(' ') : ''}</td>
                            </tr>
                            <tr>
                                <td>Host name</td>
                                <td>{archive.hostname}</td>
                            </tr>
                            <tr>
                                <td>User name</td>
                                <td>{archive.username}</td>
                            </tr>
                            <tr>
                                <td>Chunker params</td>
                                <td>{archive.chunkerParams ? archive.chunkerParams.join(', ') : ''}</td>
                            </tr>
                            <tr>
                                <td>Limits</td>
                                <td>
                                    <table className="inline">
                                        <tbody>
                                        <tr>
                                            <td>max_archive_size</td>
                                            <td>{archive.limit ? archive.limits.max_archive_size : ''}</td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                            </tbody>
                        </Table>
                    </TabPane>
                </TabContent>
                <ConfirmModal
                    onConfirm={() => this.fetchArchive(true)}
                    title={'Are you sure?'}
                    toggle={this.toggleModal}
                    open={this.state.confirmModal}
                >
                    Are you sure you want to reload the archive info and the file system list (if already cached)?
                    <br/>
                    This is a safe option but it may take some time to re-fill the caches (on demand) again.
                </ConfirmModal>
            </React.Fragment>;

        }
        return <React.Fragment>
            <PageHeader>
                {pageHeader}
            </PageHeader>
            {content}
        </React.Fragment>;
    };

    constructor(props) {
        super(props);

        this.state = {
            repoId: this.props.match.params.repoId,
            archiveId: this.props.match.params.archiveId,
            isFetching: false,
            activeTab: '1',
            confirmModal: false
        };


        this.fetchArchive = this.fetchArchive.bind(this);
        this.toggleModal = this.toggleModal.bind(this);
    }
}

export default ArchiveView;
