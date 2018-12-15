import React from 'react'
import {Table} from 'reactstrap';
import {PageHeader} from '../../general/BootstrapComponents';
import {getRestServiceUrl, humanFileSize} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import {IconRefresh} from "../../general/IconComponents";

class RepoArchiveListView extends React.Component {

    state = {
        id: this.props.match.params.id,
        isFetching: false
    };

    componentDidMount = () => {
        this.fetchRepos();
    };


    fetchRepos = (force) => {
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('repos/repoArchiveList', {
            id: this.state.id,
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
                    repo: json
                })
            })
            .catch(() => this.setState({isFetching: false, failed: true}));
    };

    render = () => {
        let content = undefined;
        const repo = this.state.repo;
        let pageHeader = '';

        if (this.state.isFetching) {
            content = <i>Loading...</i>;
        } else if (this.state.failed) {
            content = <ErrorAlert
                title={'Cannot load Repositories'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchRepos,
                    title: 'Try again'
                }}
            />;
        } else if (this.state.repo) {
            pageHeader = `${repo.displayName}`;
            content = <React.Fragment>
                {repo.displayName}
                <div
                    className={'btn btn-outline-primary refresh-button-right'}
                    onClick={this.fetchRepos.bind(this, true)}
                >
                    <IconRefresh/>
                </div>
                <Table hover>
                    <tbody>
                    <tr>
                        <td>Name</td>
                        <td>{repo.name}</td>
                    </tr>
                    <tr>
                        <td>Id</td>
                        <td>{repo.id}</td>
                    </tr>
                    <tr>
                        <td>Location</td>
                        <td>{repo.location}</td>
                    </tr>
                    <tr>
                        <td>Security dir</td>
                        <td>{repo.securityDir}</td>
                    </tr>
                    <tr>
                        <td>Encryption</td>
                        <td>{repo.encryption.mode}</td>
                    </tr>
                    <tr>
                        <td>Cache</td>
                        <td>{repo.cache.path}</td>
                    </tr>
                    <tr>
                        <td>Cache stats</td>
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
                    </tbody>
                </Table>
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

        this.fetchRepos = this.fetchRepos.bind(this);
    }
}

export default RepoArchiveListView;
