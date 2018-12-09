import React from 'react'
import './RepoListView.css';
import {CardDeck} from 'reactstrap';
import {PageHeader} from '../../general/BootstrapComponents';
import {getRestServiceUrl} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import RepoCard from './RepoCard';
import {IconRefresh} from "../../general/IconComponents";

class RepoListView extends React.Component {


    path = getRestServiceUrl('repos');
    state = {
        isFetching: false
    };

    componentDidMount = () => {
        this.fetchRepos();
    };

    fetchRepos = () => {
        this.setState({
            isFetching: true,
            failed: false,
            repos: undefined
        });
        fetch(`${this.path}/list`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(response => response.json())
            .then(json => {
                const repos = json.map(repo => {
                    return {
                        id: repo.id,
                        name: repo.name,
                        location: repo.location,
                        lastModified: repo.last_modified
                    };
                });

                this.setState({
                    isFetching: false,
                    repos
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
                title={'Cannot load Repositories'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchRepos,
                    title: 'Try again'
                }}
            />;

        } else if (this.state.repos) {

            content = <React.Fragment>
                <div
                    className={'btn btn-outline-primary refresh-button-right'}
                    onClick={this.fetchRepos}
                >
                    <IconRefresh/>
                </div>
                <CardDeck>
                {this.state.repos.map(repo => {
                    return <RepoCard
                        key={repo.id}
                        repo={repo}
                    />;
                })}
                </CardDeck>
            </React.Fragment>;

        }

        return <React.Fragment>
            <PageHeader>
                Repositories
            </PageHeader>
            {content}
        </React.Fragment>;
    };

    constructor(props) {
        super(props);

        this.fetchRepos = this.fetchRepos.bind(this);
    }
}

export default RepoListView;
