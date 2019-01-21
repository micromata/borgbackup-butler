import React from 'react';
import {Link} from 'react-router-dom'
import {
    FormButton,
    FormField,
    FormGroup,
    FormLabel,
    FormLabelInputField,
    FormOption,
    FormRadioButton,
    FormSelect
} from '../../general/forms/FormComponents';
import I18n from "../../general/translation/I18n";
import {PageHeader} from "../../general/BootstrapComponents";

class CreateRepoPage extends React.Component {

    constructor(props) {
        super(props);
        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.state = {
            repoConfig: {},
            mode: 'existingRepo',
            localRemote: 'local',
            encryption: 'repokey'
        };
    }

    handleTextChange = event => {
        event.preventDefault();
        this.setState({repoConfig: {...this.state.repoConfig, [event.target.name]: event.target.value}});
    }

    handleCheckboxChange = event => {
        this.setState({[event.target.name]: event.target.value});
    }

    render() {
        const repoConfig = this.state.repoConfig;
        return <React.Fragment>
            <PageHeader>
                Configure new repository
            </PageHeader>
            <form>
                <FormGroup>
                    <FormLabel length={2}>{'Mode'}</FormLabel>
                    <FormField length={10}>
                        <FormRadioButton name={'mode'} id={'mode1'} label={'Add existing repository'}
                                         value={'existingRepo'}
                                         checked={this.state.mode === 'existingRepo'}
                                         onChange={this.handleCheckboxChange}
                                         hint={'Do you want to add an already existing Borg repository?'}/>
                        <FormRadioButton name={'mode'} id={'mode2'} label={'Init new repository'} value={'initNewRepo'}
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
                                     onChange={this.handleTextChange}
                                     placeholder="Enter display name (only for displaying purposes)."/>
                <FormLabelInputField label={'Repo'} fieldLength={12}
                                     name={'repo'} value={repoConfig.repo}
                                     onChange={this.handleTextChange}
                                     placeholder="Enter the name of the repo, used by Borg."/>
                <FormLabelInputField label={'RSH'} fieldLength={12}
                                     name={'rsh'} value={repoConfig.rsh}
                                     onChange={this.handleTextChange}
                                     placeholder="Enter the rsh value (ssh command) for remote repository."
                                     className={this.state.localRemote === 'local' ? 'hidden' : null}/>
                <FormGroup className={this.state.mode === 'existingRepo' ? 'hidden' : null}>
                    <FormLabel length={2}>{'Encryption'}</FormLabel>
                    <FormField length={2}>
                        <FormSelect
                            value={repoConfig.encryption}
                            name={'encryption'}
                            onChange={this.handleTextChange}
                            hint={'Encryption for the new repository (use repokey if you don\'t know what to choose).'}
                        >
                            <FormOption label={'repokey (SHA256)'} value={'repokey'} hint={'hurzel'}/>
                            <FormOption label={'repokey-blake2'} value={'repokey-blake2'}/>
                            <FormOption label={'keyfile (SHA256)'} value={'keyfile'}/>
                            <FormOption label={'none (not recommended)'} value={'none'}/>
                        </FormSelect>
                    </FormField>
                </FormGroup>
                <FormLabelInputField label={'Password command'} fieldLength={12}
                                     name={'passwordCommand'} value={repoConfig.passwordCommand}
                                     onChange={this.handleTextChange}
                                     placeholder="Enter the password command to get the command from."/>
                <FormLabelInputField label={'Password'} fieldLength={6} type={'password'}
                                     name={'passphrase'} value={repoConfig.passphrase}
                                     onChange={this.handleTextChange}
                                     hint={"It's recommended to use password command instead."}
                />
                <FormField length={12}>
                    <Link to={'/repos'} className={'btn btn-outline-primary'}><I18n name={'common.cancel'}/>
                    </Link>
                    <FormButton onClick={this.onSave} bsStyle="primary"
                                hintKey="configuration.save.hint"><I18n name={'common.save'}/>
                    </FormButton>
                </FormField>
            </form>
            <code>
                <h2> Please
                    note:</h2>
                <ul>
                    <li> Not yet implemented.</li>
                    <li> This page is under construction.</li>
                    <li> Please add configuration of repository manually in borg butler json config - file.
                    </li>
                </ul>
            </code>
        </React.Fragment>;
    }
}

export default CreateRepoPage;

