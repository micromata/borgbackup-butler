import React from 'react'
import {getRestServiceUrl, humanFileSize} from '../../../utilities/global';
import ErrorAlert from '../../general/ErrorAlert';
import {IconRefresh} from "../../general/IconComponents";

class ArchiveView extends React.Component {

    state = {
        isFetching: false, activeTab: '1',
    };

    componentDidMount = () => {
        this.fetchArchiveFileList();
    };


    fetchArchiveFileList = (force) => {
        let forceReload = false;
        if (force && confirm('Are you sure you want to reload the archive file list? This may take a long time...')) {
            forceReload = true;
        }
        this.setState({
            isFetching: true,
            failed: false
        });
        fetch(getRestServiceUrl('repos/archive', {
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
