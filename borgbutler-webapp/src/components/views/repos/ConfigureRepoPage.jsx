import React from 'react';
import {Link} from 'react-router-dom'
import {
    FormButton,
    FormField,
    FormGroup,
    FormLabel,
    FormOption,
    FormRadioButton,
    FormSelect
} from '../../general/forms/FormComponents';
import I18n from '../../general/translation/I18n';
import {PageHeader} from '../../general/BootstrapComponents';
import RepoConfigBasePanel from './RepoConfigBasePanel';
import RepoConfigPasswordPanel from './RepoConfigPasswordPanel';

class ConfigureRepoPage extends React.Component {

    constructor(props) {
        super(props);
        this.handleRepoConfigChange = this.handleRepoConfigChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.setRepoValue = this.setRepoValue.bind(this);
        this.state = {
            repoConfig: {
                encryption: 'repoKey'
            },
            mode: 'existingRepo',
            localRemote: 'local'
        };
    }

    handleRepoConfigChange = event => {
        event.preventDefault();
        //console.log(event.target.name + ": " + event.target.value);
        this.setState({repoConfig: {...this.state.repoConfig, [event.target.name]: event.target.value}});
    }

    setRepoValue(variable, value) {
        //console.log(variable + "=" + value);
        this.setState({repoConfig: {...this.state.repoConfig, [variable]: value}})
    }

    handleCheckboxChange = event => {
        this.setState({[event.target.name]: event.target.value});
        /*if (event.target.name === 'mode' && event.target.value === 'existingRepo'
            && this.state.passwordMethod !== 'passwordCommand' && this.state.passwordMethod !== 'passphrase') {
            // Other options such as Mac OS key chain isn't available for existing repos:
            this.setState({passwordMethod: 'passwordCommand'});
        }*/
    }

    render() {
        const repoConfig = this.state.repoConfig;
        return <React.Fragment>
            <PageHeader>
                Configure repository
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
                <RepoConfigBasePanel repoConfig={repoConfig}
                                     remote={this.state.localRemote !== 'local'}
                                     handleRepoConfigChange={this.handleRepoConfigChange}
                                     setRepoValue={this.setRepoValue}/>
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
                <RepoConfigPasswordPanel encryption={this.state.repoConfig.encryption}
                                         repoConfig={repoConfig}
                                         handleRepoConfigChange={this.handleRepoConfigChange}
                                         setRepoValue={this.setRepoValue}/>
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

export default ConfigureRepoPage;
