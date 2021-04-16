import React from 'react'
import {Badge, Nav, NavLink, TabContent, Table, TabPane} from 'reactstrap';
import {Link} from "react-router-dom";
import classNames from 'classnames';
import {PageHeader} from '../../general/BootstrapComponents';
import {getRestServiceUrl, humanFileSize} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import {IconCheck, IconRefresh} from '../../general/IconComponents';
import JobMonitorPanel from '../jobs/JobMonitorPanel';
import RepoConfigPanel from "./RepoConfigPanel";

class RepoArchiveListView extends React.Component {

    state = {
        id: this.props.match.params.id,
        displayName: this.props.match.params.displayName,
        isFetching: false,
        activeTab: '1',
        redirectOnError: true
    };

    componentDidMount = () => {
        this.fetchRepo();
    };


    fetchRepo = (force) => {
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('repos/repoArchiveList', {
            id: this.state.id,
            force: force === true,
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
                    repo: json
                })
            })
            .catch(() => {
                this.setState({isFetching: false, failed: true})
                if (this.state.redirectOnError && this.state.activeTab !== '3') {
                    this.setState({activeTab: '3', redirectOnError: false});
                }
            });
    };

    toggleTab = tab => () => {
        this.setState({
            activeTab: tab
        })
    };

    afterSave() {
        if (!this.state.failed) {
            this.setState({
                activeTab: '1'
            })
        }
    }

    afterCancel() {
        if (!this.state.failed) {
            this.setState({
                activeTab: '1'
            })
        }
    }

    afterRemove() {
        this.props.history.push('/repos');
    }


    render = () => {
        let errorBadge = '';
        let content1 = undefined;
        let content2 = undefined;
        const repo = this.state.repo;
        const displayName = (this.state.displayName) ? this.state.displayName : `Error: id=${this.state.id}`;
        let pageHeader = <React.Fragment>
            {displayName}
        </React.Fragment>;

        if (this.state.isFetching) {
            content1 = <JobMonitorPanel repo={this.state.id}/>;
            content2 = content1;
        } else if (this.state.failed) {
            content1 = <ErrorAlert
                title={'Cannot load Repositories'}
                description={'Something went wrong, may-be wrong configuration?'}
                action={{
                    handleClick: this.fetchRepo,
                    title: 'Try again'
                }}
            />;
            content2 = content1;
            errorBadge = <Badge color="danger" pill>!</Badge>;
        } else if (this.state.repo) {
            pageHeader = <React.Fragment>
                {repo.displayName}
                <div
                    className={'btn btn-outline-primary refresh-button-right'}
                    onClick={this.fetchRepo.bind(this, true)}
                >
                    <IconRefresh/>
                </div>
            </React.Fragment>;
            let stats = '';
            if (repo.cache && repo.cache.stats) {
                stats = <tr>
                    <td>Stats</td>
                    <td>
                        <table className="inline">
                            <tbody>
                            <tr>
                                <td>Total chunks</td>
                                <td>{Number(repo.cache.stats.total_chunks).toLocaleString()}</td>
                            </tr>
                            <tr>
                                <td>Total csize</td>
                                <td>{humanFileSize(repo.cache.stats.total_csize)}</td>
                            </tr>
                            <tr>
                                <td>Total size</td>
                                <td>{humanFileSize(repo.cache.stats.total_size)}</td>
                            </tr>
                            <tr>
                                <td>Total unique chunks</td>
                                <td>{Number(repo.cache.stats.total_unique_chunks).toLocaleString()}</td>
                            </tr>
                            <tr>
                                <td>Unique csize</td>
                                <td>{humanFileSize(repo.cache.stats.unique_csize)}</td>
                            </tr>
                            <tr>
                                <td>Unique size</td>
                                <td>{humanFileSize(repo.cache.stats.unique_size)}</td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
            }
            let encryption = '';
            if (repo.encryption) {
                encryption = <tr>
                    <td>Encryption</td>
                    <td>{repo.encryption.mode}</td>
                </tr>
            }
            let cachePath = '';
            if (repo.cache) {
                cachePath = <tr>
                    <td>Cache</td>
                    <td>{repo.cache.path}</td>
                </tr>
            }

            if (repo.archives) {
                content1 = <Table hover>
                    <tbody>
                    <tr>
                        <th>Archive</th>
                        <th>Time</th>
                        <th></th>
                        <th>Id</th>
                    </tr>
                    {repo.archives.map((archive) => {
                        // Return the element. Also pass key
                        let loaded = '';
                        if (archive.fileListAlreadyCached) {
                            loaded = <IconCheck/>;
                        }
                        return (
                            <tr key={archive.id}>
                                <td><Link to={`/archives/${repo.id}/${archive.id}/`}>{archive.name}</Link></td>
                                <td>{archive.time}</td>
                                <td>{loaded}</td>
                                <td>{archive.id}</td>
                            </tr>);
                    })}
                    </tbody>
                </Table>;
            } else {
                content1 = 'No archives available.';
            }
            content2 = <Table striped bordered hover>
                <tbody>
                <tr>
                    <td>Id</td>
                    <td>{repo.id}</td>
                </tr>
                <tr>
                    <td>Name</td>
                    <td>{repo.name}</td>
                </tr>
                <tr>
                    <td>Location</td>
                    <td>{repo.location}</td>
                </tr>
                {stats}
                <tr>
                    <td>Security dir</td>
                    <td>{repo.securityDir}</td>
                </tr>
                {encryption}
                {cachePath}
                </tbody>
            </Table>;

        }
        return <React.Fragment>
            <PageHeader>
                <Link to={'/repos'}> Repositories</Link> - {pageHeader}
            </PageHeader>
            <Nav tabs>
                <NavLink
                    className={classNames({active: this.state.activeTab === '1'})}
                    onClick={this.toggleTab('1')}
                >
                    Archives {errorBadge}
                </NavLink>
                <NavLink
                    className={classNames({active: this.state.activeTab === '2'})}
                    onClick={this.toggleTab('2')}
                >
                    Information {errorBadge}
                </NavLink>
                <NavLink
                    className={classNames({active: this.state.activeTab === '3'})}
                    onClick={this.toggleTab('3')}
                >
                    Configuration {errorBadge}
                </NavLink>
            </Nav>
            <TabContent activeTab={this.state.activeTab}>
                <TabPane tabId={'1'}>
                    {content1}
                </TabPane>
                <TabPane tabId={'2'}>
                    {content2}
                </TabPane>
                <TabPane tabId={'3'}>
                    <RepoConfigPanel id={this.state.id}
                                     afterCancel={this.afterCancel}
                                     afterSave={this.afterSave}
                                     afterRemove={this.afterRemove}
                                     repoError={this.state.failed}/>
                </TabPane>
            </TabContent>
        </React.Fragment>;
    };

    constructor(props) {
        super(props);

        this.fetchRepo = this.fetchRepo.bind(this);
        this.toggleTab = this.toggleTab.bind(this);
        this.afterCancel = this.afterCancel.bind(this);
        this.afterSave = this.afterSave.bind(this);
        this.afterRemove = this.afterRemove.bind(this);
    }
}

export default RepoArchiveListView;
