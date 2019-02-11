import React from 'react';
import {FormButton, FormField, FormGroup, FormLabel} from '../../general/forms/FormComponents';
import {getRestServiceUrl} from '../../../utilities/global';
import I18n from "../../general/translation/I18n";
import LoadingOverlay from '../../general/loading/LoadingOverlay';
import PropTypes from "prop-types";
import ErrorAlert from "../../general/ErrorAlert";
import RepoConfigBasePanel from './RepoConfigBasePanel';
import RepoConfigPasswordPanel from './RepoConfigPasswordPanel';
import RepoConfigTestPanel from './RepoConfigTestPanel';
import ConfirmModal from '../../general/modal/ConfirmModal';

class RepoConfigPanel extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            repoConfig: undefined,
            confirmModal: false
        };

        this.fetch = this.fetch.bind(this);
        this.setRepoValue = this.setRepoValue.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onRemove = this.onRemove.bind(this);
        this.onCancel = this.onCancel.bind(this);
        this.toggleModal = this.toggleModal.bind(this);
    }

    componentDidMount = () => this.fetch();

    toggleModal() {
        this.setState({
            confirmModal: !this.state.confirmModal
        })
    }

    fetch = () => {
        this.setState({
            isFetching: true,
            failed: false,
            repoConfig: undefined
        });
        fetch(getRestServiceUrl('repoConfig', {
            id: this.props.id
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
                    repoConfig: json
                })
            })
            .catch((error) => {
                console.log("error", error);
                this.setState({
                    isFetching: false,
                    failed: true
                });
            })
    };

    handleRepoConfigChange = event => {
        event.preventDefault();
        this.setRepoValue(event.target.name, event.target.value);
    }

    setRepoValue(variable, value) {
        //console.log(variable + "=" + value);
        this.setState({
            repoConfig: {...this.state.repoConfig, [variable]: value},
        })
    }

    onRemove(event) {
        const response = fetch(getRestServiceUrl('repoConfig/remove', {
            id: this.props.id
        }), {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(response => response.text())
            .then(text => {
            })
            .catch((error) => {
                console.log("error", error);
            })
    }

    async onSave(event) {
        const response = fetch(getRestServiceUrl("repoConfig"), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(this.state.repoConfig)
        });
        if (response) await response;
        if (this.props.afterSave) {
            this.props.afterSave();
        }
    }

    async onCancel() {
        const response = this.fetch();
        if (response) await response;
        if (this.props.afterCancel) {
            this.props.afterCancel();
        }
    }

    render() {
        let content;
        let repoError = '';
        if (this.props.repoError) {
            repoError = <ErrorAlert
                title={'Internal error'}
                description={'Repo not available or mis-configured (please refer the log files for more details).'}
            />
        }
        if (this.state.isFetching) {
            content = <React.Fragment>Loading...</React.Fragment>;
        } else if (this.state.failed) {
            content = <ErrorAlert
                title={'Cannot load config of repository'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchRepo,
                    title: 'Try again'
                }}
            />;
        } else if (this.state.repoConfig) {
            const repoConfig = this.state.repoConfig;
            const remote = (repoConfig.rsh && repoConfig.rsh.length > 0) ||
                (repoConfig.repo && (repoConfig.repo.indexOf('@') >= 0 || repoConfig.repo.indexOf('ssh://') >= 0));
            content = <React.Fragment>
                <RepoConfigBasePanel repoConfig={repoConfig}
                                     remote={remote}
                                     handleRepoConfigChange={this.handleRepoConfigChange}
                                     setRepoValue={this.setRepoValue}/>
                <RepoConfigPasswordPanel passwordMethod={'auto'}
                                         repoConfig={repoConfig}
                                         handleRepoConfigChange={this.handleRepoConfigChange}
                                         setRepoValue={this.setRepoValue}/>
                <FormGroup>
                    <FormLabel length={2}/>
                    <FormField length={10}>
                        <FormButton onClick={this.onCancel}
                                    hintKey="configuration.cancel.hint"><I18n name={'common.cancel'}/>
                        </FormButton>
                        <FormButton onClick={this.toggleModal} bsStyle={'outline-danger'}>Remove</FormButton>
                        <FormButton onClick={this.onSave} bsStyle="primary"
                                    hintKey="configuration.save.hint"><I18n name={'common.save'}/>
                        </FormButton>
                    </FormField>
                </FormGroup>
                <RepoConfigTestPanel repoConfig={this.state.repoConfig}
                                     repoError={this.props.repoError}/>
                <LoadingOverlay active={this.state.loading}/>
            </React.Fragment>;
        }
        return <React.Fragment>
            <ConfirmModal
                onConfirm={this.onRemove}
                title={'Are you sure?'}
                toggle={this.toggleModal}
                open={this.state.confirmModal}
            >
                Do you really want to remove this repository from BorgButler?
                <br/>
                The Borg repository itself and its content will be left untouched.
            </ConfirmModal>
            {content}
            {repoError}
        </React.Fragment>;
    }
}

RepoConfigPanel.propTypes = {
    afterCancel: PropTypes.func.isRequired,
    afterSave: PropTypes.func.isRequired,
    id: PropTypes.string,
    repoError: PropTypes.bool
};

export default RepoConfigPanel;

