import React from 'react';
import {FormGroup} from 'reactstrap';
import {FormButton, FormField} from '../../general/forms/FormComponents';
import {getRestServiceUrl} from '../../../utilities/global';
import I18n from "../../general/translation/I18n";
import LoadingOverlay from '../../general/loading/LoadingOverlay';
import PropTypes from "prop-types";
import ErrorAlert from "../../general/ErrorAlert";
import RepoConfigBasePanel from './RepoConfigBasePanel';
import RepoConfigPasswordPanel from './RepoConfigPasswordPanel';

class RepoConfigPanel extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            repoConfig: undefined
        };

        this.fetch = this.fetch.bind(this);
        this.setRepoValue = this.setRepoValue.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onCancel = this.onCancel.bind(this);
    }

    componentDidMount = () => this.fetch();

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
        this.setState({repoConfig: {...this.state.repoConfig, [variable]: value}})
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
                title={'Cannot access repository'}
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
            let encryption = 'undefined'; // Todo
            content = <React.Fragment>
                <RepoConfigBasePanel repoConfig={repoConfig}
                                     handleRepoConfigChange={this.handleRepoConfigChange}
                                     setRepoValue={this.setRepoValue}/>
                <RepoConfigPasswordPanel encryption={encryption}
                                         repoConfig={repoConfig}
                                         handleRepoConfigChange={this.handleRepoConfigChange}
                                         setRepoValue={this.setRepoValue}/>
                <FormGroup>
                    <FormField length={12}>
                        <FormButton onClick={this.onCancel}
                                    hintKey="configuration.cancel.hint"><I18n name={'common.cancel'}/>
                        </FormButton>
                        <FormButton onClick={this.onSave} bsStyle="primary"
                                    hintKey="configuration.save.hint"><I18n name={'common.save'}/>
                        </FormButton>
                    </FormField>
                </FormGroup>
                <LoadingOverlay active={this.state.loading}/>
            </React.Fragment>;
        }
        return <React.Fragment>{content}{repoError}</React.Fragment>;
    }
}

RepoConfigPanel.propTypes = {
    afterCancel: PropTypes.func.isRequired,
    afterSave: PropTypes.func.isRequired,
    id: PropTypes.string,
    repoError: PropTypes.bool
};

export default RepoConfigPanel;

