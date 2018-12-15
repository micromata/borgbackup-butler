import React from 'react'
import {PageHeader} from '../../general/BootstrapComponents';
import {getRestServiceUrl} from '../../../utilities/global';
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
