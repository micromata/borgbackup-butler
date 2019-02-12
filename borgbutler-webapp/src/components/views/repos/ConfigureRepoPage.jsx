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
import RepoConfigTestPanel from './RepoConfigTestPanel';
import {getRestServiceUrl} from "../../../utilities/global";

class ConfigureRepoPage extends React.Component {

    constructor(props) {
        super(props);
        this.onSave = this.onSave.bind(this);
        this.handleRepoConfigChange = this.handleRepoConfigChange.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.setRepoValue = this.setRepoValue.bind(this);
        this.state = {
            repoConfig: {id: 'new'},
            encryption: 'repoKey',
            mode: 'existingRepo',
            localRemote: 'local'
        };
    }

    onSave(event) {
        this.setState(
            {repoConfig: {...this.state.repoConfig, id: this.state.mode == 'initNewRepo' ? 'init' : 'new'}},
            () => {
                const response = fetch(getRestServiceUrl("repoConfig"), {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(this.state.repoConfig)
                })
                    .then(json => {
                        this.props.history.push('/repos');
                    })
                    .catch(() => {
                    });
            }
        )
    }

    handleRepoConfigChange = event => {
        event.preventDefault();
        this.setRepoValue(event.target.name, event.target.value);
    }

    setRepoValue(variable, value) {
        this.setState({repoConfig: {...this.state.repoConfig, [variable]: value}})
    }

    handleInputChange = (event) => {
        event.preventDefault();
        this.setState({[event.target.name]: event.target.value});
    };

    handleCheckboxChange = event => {
        this.setState({[event.target.name]: event.target.value});
    }

    render() {
        const repoConfig = this.state.repoConfig;
        const saveButtonLabel = this.state.mode === 'initNewRepo' ? 'Init and save' : <I18n name={'common.save'}/>;
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
                            value={this.state.encryption}
                            name={'encryption'}
                            onChange={this.handleInputChange}
                            hint={'Encryption for the new repository (use repokey if you don\'t know what to choose).'}
                        >
                            <FormOption label={'repokey (SHA256)'} value={'repokey'}/>
                            <FormOption label={'repokey-blake2'} value={'repokey-blake2'}/>
                            <FormOption label={'keyfile (SHA256)'} value={'keyfile'}/>
                            <FormOption label={'none (not recommended)'} value={'none'}/>
                        </FormSelect>
                    </FormField>
                </FormGroup>
                <RepoConfigPasswordPanel passwordMethod={this.state.encryption === 'none' ? 'none' : 'yes'}
                                         repoConfig={repoConfig}
                                         handleRepoConfigChange={this.handleRepoConfigChange}
                                         setRepoValue={this.setRepoValue}/>
                <FormGroup>
                    <FormLabel length={2}/>
                    <FormField length={10}>
                        <Link to={'/repos'} className={'btn btn-outline-primary'}><I18n name={'common.cancel'}/>
                        </Link>
                        <FormButton onClick={this.onSave} bsStyle="primary"
                                    disabled={repoConfig.repo && repoConfig.repo.length > 0
                                    && repoConfig.displayName && repoConfig.displayName.length > 0 ? false : true}
                                    hintKey="configuration.save.hint">{saveButtonLabel}
                        </FormButton>
                    </FormField>
                </FormGroup>
                <RepoConfigTestPanel repoConfig={this.state.repoConfig}/>
            </form>
        </React.Fragment>;
    }
}

export default ConfigureRepoPage;
