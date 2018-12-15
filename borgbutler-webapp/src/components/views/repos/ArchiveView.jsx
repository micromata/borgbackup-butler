import React from 'react'
import {Table} from 'reactstrap';
import {PageHeader} from '../../general/BootstrapComponents';
import {getRestServiceUrl} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import {IconRefresh} from "../../general/IconComponents";

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
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('repos/archive', {
            repo: this.state.repoId,
            archive: this.state.archiveId,
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

    render = () => {
        let content = undefined;
        const archive = this.state.archive;
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
        } else if (this.state.repo) {
            pageHeader = <React.Fragment>
                {archive.id}
                <div
                    className={'btn btn-outline-primary refresh-button-right'}
                    onClick={this.fetchArchive.bind(this, true)}
                >
                    <IconRefresh/>
                </div>
            </React.Fragment>;
            content = <React.Fragment>
                <Table hover>
                    <tbody>
                    <tr>
                        <th>Archive</th>
                        <th>Time</th>
                        <th>Id</th>
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

        this.fetchArchive = this.fetchArchive.bind(this);
    }
}

export default ArchiveView;
