import React from 'react';
import {Link} from 'react-router-dom'
import {
    FormButton,
    FormField,
    FormGroup,
    FormInput,
    FormLabel,
    FormLabelInputField,
    FormOption,
    FormRadioButton,
    FormSelect
} from '../../general/forms/FormComponents';
import I18n from '../../general/translation/I18n';
import {getRestServiceUrl} from '../../../utilities/global';
import {PageHeader} from '../../general/BootstrapComponents';
import PropTypes from 'prop-types';
import RepoPasswordConfigPanel from './RepoPasswordConfigPanel';

class ConfigureRepoPage extends React.Component {

    constructor(props) {
        super(props);
        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleRepoConfigChange = this.handleRepoConfigChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.state = {
            repoConfig: {
                encryption: 'repoKey'
            },
            mode: 'existingRepo',
            localRemote: 'local'
        };
    }

    handleTextChange = event => {
        event.preventDefault();
        this.setState({[event.target.name]: event.target.value});
    }


    handleRepoConfigChange = event => {
        event.preventDefault();
        //console.log(event.target.name + ": " + event.target.value);
        this.setState({repoConfig: {...this.state.repoConfig, [event.target.name]: event.target.value}});
    }

    handleCheckboxChange = event => {
        this.setState({[event.target.name]: event.target.value});
        /*if (event.target.name === 'mode' && event.target.value === 'existingRepo'
            && this.state.passwordMethod !== 'passwordCommand' && this.state.passwordMethod !== 'passphrase') {
            // Other options such as Mac OS key chain isn't available for existing repos:
            this.setState({passwordMethod: 'passwordCommand'});
        }*/
    }

    browseDirectory = () => {
        const current = "&current=" + encodeURIComponent(this.state.repoConfig.repo);
        fetch(getRestServiceUrl("files/browse-local-filesystem?type=dir" + current), {
            method: "GET",
            dataType: "JSON",
            headers: {
                "Content-Type": "text/plain; charset=utf-8",
            }
        })
            .then((resp) => {
                return resp.json()
            })
            .then((data) => {
                if (data.directory) {
                    this.setState({repoConfig: {...this.state.repoConfig, repo: data.directory}});
                }
            })
            .catch((error) => {
                console.log(error, "Oups, what's happened?")
            })
    }

    render() {
        const repoConfig = this.state.repoConfig;
        let repoPlaceHolder = 'Enter the repo used by Borg.';
        if (this.state.mode === 'initNewRepo' && this.state.localRemote === 'remote') {
            repoPlaceHolder = 'Enter the remote path of the repo, such as user@hostname:backup.';
        }
        let repoFieldLength = 10;
        let browseButton = null;
        if (this.state.localRemote === 'local') {
            repoFieldLength = 9;
            browseButton = <FormButton onClick={this.browseDirectory}
                                       hint={'Browse local backup directory. (experimental!)'}><I18n
                name={'common.browse'}/>
            </FormButton>;
            repoPlaceHolder = 'Enter or browse the local path of the repo home dir used by Borg.';
        }
        return <React.Fragment>
            <PageHeader>
                Configure repository
            </PageHeader>
            <form>
                <FormGroup className={this.props.editExistingRepo ? 'hidden' : null}>
                    <FormLabel length={2}>{'Mode'}</FormLabel>
                    <FormField length={10}>
                        <FormRadioButton name={'mode'} id={'mode1'} label={'Add existing repository'}
                                         value={'existingRepo'}
                                         checked={this.state.mode === 'existingRepo'}
                                         onChange={this.handleCheckboxChange}
                                         hint={'Do you want to add an already existing Borg repository?'}/>
                        <FormRadioButton name={'mode'} id={'mode2'} label={'Init new repository (not yet implemented)'}
                                         value={'initNewRepo'}
                                         checked={this.state.mode === 'initNewRepo'}
                                         onChange={this.handleCheckboxChange}
                                         hint={'Do you want to create and init a new one?'}/>
                    </FormField>
                </FormGroup>
                <FormGroup>
                    <FormLabel length={2}>{'Local or remote'}</FormLabel>
                    <FormField length={10}>
                        <FormRadioButton name={'localRemote'} id={'localRemote1'} label={'Local repository'}
                                         value={'local'}
                                         checked={this.state.localRemote === 'local'}
                                         onChange={this.handleCheckboxChange}/>
                        <FormRadioButton name={'localRemote'} id={'localRemote2'} label={'Remote repository'}
                                         value={'remote'}
                                         checked={this.state.localRemote === 'remote'}
                                         onChange={this.handleCheckboxChange}/>
                    </FormField>
                </FormGroup>
                <FormLabelInputField label={'Display name'} fieldLength={12}
                                     name={'displayName'} value={repoConfig.displayName}
                                     onChange={this.handleRepoConfigChange}
                                     placeholder="Enter display name (only for displaying purposes)."/>
                <FormGroup>
                    <FormLabel length={2}>{'Repo'}</FormLabel>
                    <FormField length={repoFieldLength}>
                        <FormInput
                            id={'repo'}
                            name={'repo'}
                            type={'text'}
                            value={repoConfig.repo}
                            onChange={this.handleRepoConfigChange}
                            placeholder={repoPlaceHolder}
                        />
                    </FormField>
                    {browseButton}
                </FormGroup>
                <FormLabelInputField label={'RSH'} fieldLength={12}
                                     name={'rsh'} value={repoConfig.rsh}
                                     onChange={this.handleRepoConfigChange}
                                     placeholder="Enter the rsh value (ssh command) for remote repository."
                                     className={this.state.localRemote === 'local' ? 'hidden' : null}/>
                <FormGroup className={this.state.mode === 'existingRepo' ? 'hidden' : null}>
                    <FormLabel length={2}>{'Encryption'}</FormLabel>
                    <FormField length={4}>
                        <FormSelect
                            value={repoConfig.encryption}
                            name={'encryption'}
                            onChange={this.handleRepoConfigChange}
                            hint={'Encryption for the new repository (use repokey if you don\'t know what to choose).'}
                        >
                            <FormOption label={'repokey (SHA256)'} value={'repokey'}/>
                            <FormOption label={'repokey-blake2'} value={'repokey-blake2'}/>
                            <FormOption label={'keyfile (SHA256)'} value={'keyfile'}/>
                            <FormOption label={'none (not recommended)'} value={'none'}/>
                        </FormSelect>
                    </FormField>
                </FormGroup>
                <RepoPasswordConfigPanel encryption={this.state.repoConfig.encryption}
                                         repoConfig={repoConfig}
                                         handleRepoConfigChange={this.handleRepoConfigChange}/>
                <FormField length={12}>
                    <Link to={'/repos'} className={'btn btn-outline-primary'}><I18n name={'common.cancel'}/>
                    </Link>
                    <FormButton onClick={this.onSave} bsStyle="primary"
                                disabled={repoConfig.repo && repoConfig.repo.length > 0
                                && repoConfig.displayName && repoConfig.displayName.length > 0 ? false : true}
                                hintKey="configuration.save.hint"><I18n name={'common.save'}/>
                    </FormButton>
                </FormField>
            </form>
            <code>
                <h2>Todo:</h2>
                <ul>
                    <li>Implement 'Save' button ;-)</li>
                    <li>Add own environment variables.</li>
                    <li>Remove and clone repo.</li>
                </ul>
            </code>
        </React.Fragment>;
    }
}

ConfigureRepoPage.propTypes = {
    // true: The user wants to edit an already existing borg repository in the config file, if false, the user wants to configure
    // a new repo and add this to the the BorgButler's config file.
    editExistingRepo: PropTypes.bool
};

ConfigureRepoPage.defaultProps = {
    editExistingRepo: false
};

export default ConfigureRepoPage;
