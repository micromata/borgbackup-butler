import React from 'react';
import {FormGroup, Input} from 'reactstrap';
import {
    FormButton,
    FormField,
    FormInput,
    FormLabelField,
    FormLabelInputField
} from '../../general/forms/FormComponents';
import {getRestServiceUrl} from '../../../utilities/global';
import I18n from "../../general/translation/I18n";
import LoadingOverlay from '../../general/loading/LoadingOverlay';
import PropTypes from "prop-types";
import ErrorAlert from "../../general/ErrorAlert";

class RepoConfigPanel
    extends React
        .Component {

    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            repoConfig: undefined
        };

        this.fetch = this.fetch.bind(this);
        this.handleTextChange = this.handleTextChange.bind(this);
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
        fetch(getRestServiceUrl('repos/repoConfig', {
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

    handleTextChange = event => {
        event.preventDefault();
        this.setState({[event.target.name]: event.target.value});
    }

    onSave(event) {
        this.setState({
            loading: true
        })
        this.setState({
            loading: false
        })
        this.setReload();
    }

    onCancel() {
        this.setReload();
    }

    render() {
        let content;
        if (this.state.isFetching) {
            content = <React.Fragment>Loading...</React.Fragment>;
        } else if (this.state.failed) {
            content = <ErrorAlert
                title={'Cannot load config or repository'}
                description={'Something went wrong during contacting the rest api.'}
                action={{
                    handleClick: this.fetchRepo,
                    title: 'Try again'
                }}
            />;
        } else if (this.state.repoConfig) {
            const repoConfig = this.state.repoConfig;
            content = <React.Fragment>
                <FormGroup>
                    <FormLabelInputField label={'Display name'} fieldLength={12}
                                         name={'displayName'} value={repoConfig.displayName}
                                         onChange={this.handleTextChange}
                                         placeholder="Enter display name (only for displaying purposes)."/>
                    <FormLabelInputField label={'Repo'} fieldLength={12}
                                         name={'repo'} value={repoConfig.repo}
                                         onChange={this.handleTextChange}
                                         placeholder="Enter the name of the repo, used by Borg."/>
                    <FormLabelInputField label={'RSH'} fieldLength={12}
                                         name={'rsh'} value={repoConfig.rsh}
                                         onChange={this.handleTextChange}
                                         placeholder="Enter the rsh value (ssh command) for remote repository."/>
                    <FormLabelInputField label={'Password command'} fieldLength={12}
                                         name={'passwordCommand'} value={repoConfig.passwordCommand}
                                         onChange={this.handleTextChange}
                                         placeholder="Enter the password command to get the command from."/>
                    <FormLabelInputField label={'Password'} fieldLength={6} type={'password'}
                                         name={'passwordCommand'} value={repoConfig.password}
                                         onChange={this.handleTextChange}
                                         hint={"It's recommended to use password command instead."}
                    />
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
        return <React.Fragment>{content}</React.Fragment>;
    }
}

RepoConfigPanel
    .propTypes = {
    id: PropTypes.string
};

export default RepoConfigPanel;

