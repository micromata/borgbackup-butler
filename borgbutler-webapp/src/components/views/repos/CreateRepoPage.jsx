import React from 'react';
import {Link} from 'react-router-dom'
import {
    FormButton,
    FormField,
    FormGroup,
    FormLabel,
    FormLabelInputField,
    FormOption,
    FormSelect
} from '../../general/forms/FormComponents';
import I18n from "../../general/translation/I18n";
import {PageHeader} from "../../general/BootstrapComponents";

class CreateRepoPage extends React.Component {

    constructor(props) {
        super(props);
        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleSelectChange = this.handleSelectChange.bind(this);
        this.state = {
            repoConfig: {},
            mode: 'existingRepo'
        };
    }

    handleTextChange = event => {
        event.preventDefault();
        this.setState({repoConfig: {...this.state.repoConfig, [event.target.name]: event.target.value}});
    }

    handleSelectChange = event => {
        event.preventDefault();
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
                    <FormField length={4}>
                        <FormSelect
                            value={this.state.mode}
                            name={'mode'}
                            onChange={this.handleSelectChange}
                            hint={'Do you want to add an already existing Borg repository or do you want to create a new one?'}
                        >
                            <FormOption label={'Add existing repo'} value={'existingRepo'}/>
                            <FormOption label={'Create new repo'} value={'newRepo'}/>
                        </FormSelect>
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
                                     placeholder="Enter the rsh value (ssh command) for remote repository."/>
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

