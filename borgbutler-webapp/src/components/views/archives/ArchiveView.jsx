import React from 'react'
import {Nav, NavLink, TabContent, Table, TabPane} from 'reactstrap';
import {PageHeader} from '../../general/BootstrapComponents';
import {getRestServiceUrl, humanFileSize, humanSeconds} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import {IconRefresh} from "../../general/IconComponents";
import classNames from "classnames";
import FileListPanel from "./FileListPanel";

class ArchiveView extends React.Component {

    state = {
        repoId: this.props.match.params.repoId,
        archiveId: this.props.match.params.archiveId,
        isFetching: false,
        activeTab: '1',
    };

    componentDidMount = () => {
        this.fetchArchive();
    };


    fetchArchive = (force) => {
        let forceReload = false;
        if (force && window.confirm('Are you sure you want to reload the archive info? This may take some time...')) {
            forceReload = true;
        }
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('archives', {
            repo: this.state.repoId,
            archive: this.state.archiveId,
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

    toggleTab = tab => () => {
        this.setState({
            activeTab: tab
        })
    };

    render = () => {
        let content = undefined;
        let archive = this.state.archive;
        let pageHeader = '';

        if (this.state.isFetching) {
            content = <i>Loading...</i>;
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
                {archive.repoDisplayName}
                <div
                    className={'btn btn-outline-primary refresh-button-right'}
                    onClick={this.fetchArchive.bind(this, true)}
                >
                    <IconRefresh/>
                </div>
            </React.Fragment>;
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
                <TabPane tabId={'1'}>
                    <FileListPanel
                        archiveId={archive.id}
                    />
                </TabPane>
                <TabContent activeTab={this.state.activeTab}>
                    <TabPane tabId={'2'}>
                        <Table hover>
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
                            <tr>
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
                            <tr>
                                <td>Comment</td>
                                <td>{archive.comment}</td>
                            </tr>
                            <tr>
                                <td>Command line</td>
                                <td>{archive.commandLine.join(' ')}</td>
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
                                <td>{archive.chunkerParams.join(', ')}</td>
                            </tr>
                            <tr>
                                <td>Limits</td>
                                <td>
                                    <table className="inline">
                                        <tbody>
                                        <tr>
                                            <td>max_archive_size</td>
                                            <td>{archive.limits.max_archive_size}</td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                            </tbody>
                        </Table>
                    </TabPane>
                </TabContent>
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

        this.fetchArchive = this.fetchArchive.bind(this);
    }
}

export default ArchiveView;
