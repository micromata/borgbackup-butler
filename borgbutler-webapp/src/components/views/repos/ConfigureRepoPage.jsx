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
import I18n from "../../general/translation/I18n";
import {PageHeader} from "../../general/BootstrapComponents";

class ConfigureRepoPage extends React.Component {

    constructor(props) {
        super(props);
        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleRepoTextChange = this.handleRepoTextChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.state = {
            repoConfig: {},
            mode: 'existingRepo',
            localRemote: 'local',
            encryption: 'repokey',
            passwordMethod: 'passwordCommand',
            passwordCreate: null
        };
    }

    handleTextChange = event => {
        event.preventDefault();
        this.setState({[event.target.name]: event.target.value});
        if (event.target.name === 'passwordMethod') {
            const value = event.target.value;
            var passwordCommand = null;
            var passwordCreate = null;
            if (value === 'passwordFile') {
                passwordCommand = 'cat ~/.borg-passphrase';
                passwordCreate = <React.Fragment>
                    Create a file with a password in it in your home directory and use permissions to keep anyone else
                    from
                    reading it:<br/>
                    <div className="command-line">head -c 1024 /dev/urandom | base64 > ~/.borg-passphrase<br/>
                        chmod 400 ~/.borg-passphrase
                    </div>
                </React.Fragment>;
            } else if (value === 'macos-keychain') {
                passwordCommand = 'security find-generic-password -a $USER -s borg-passphrase';
                passwordCreate = <React.Fragment>
                    Generate a passphrase and use security to save it to your login (default) keychain:<br/>
                    <div className="command-line">security add-generic-password -D secret -U -a $USER -s borg-passphrase
                        -w $(head -c 1024 /dev/urandom | base64)
                    </div>
                </React.Fragment>;
            } else if (value === 'gnome-keyring') {
                passwordCommand = 'secret-tool lookup borg-repository repo-name';
                passwordCreate = <React.Fragment>
                    First ensure libsecret-tools, gnome-keyring and libpam-gnome-keyring are installed. If
                    libpam-gnome-keyring wasn’t already installed, ensure it runs on login:<br/>
                    <div className="command-line">sudo sh -c "echo session optional pam_gnome_keyring.so auto_start >>
                        /etc/pam.d/login"<br/>
                        sudo sh -c "echo password optional pam_gnome_keyring.so >> /etc/pam.d/passwd"<br/>
                        # you may need to relogin afterwards to activate the login keyring
                    </div>
                    Then add a secret to the login keyring:
                    <div className="command-line">head -c 1024 /dev/urandom | base64 | secret-tool store borg-repository
                        repo-name --label="Borg Passphrase"</div>
                </React.Fragment>;
            } else if (value === 'kwallet') {
                passwordCommand = 'kwalletcli -e borg-passphrase -f Passwords';
                passwordCreate = <React.Fragment>
                    Ensure kwalletcli is installed, generate a passphrase, and store it in your “wallet”:<br/>
                    <div className="command-line">head -c 1024 /dev/urandom | base64 | kwalletcli -Pe borg-passphrase -f
                        Passwords
                    </div>
                </React.Fragment>;
            }
            if (passwordCommand) {
                this.setState({repoConfig: {...this.state.repoConfig, 'passwordCommand': passwordCommand}})
            }
            this.setState({'passwordCreate': passwordCreate});
        }
    }


    handleRepoTextChange = event => {
        event.preventDefault();
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

    render() {
        const repoConfig = this.state.repoConfig;
        var passwordMethods = [['password-command', 'Password command'],
            ['passwordFile', 'Password file'],
            ['macos-keychain', 'Mac OS X keychain'],
            ['gnome-keyring', 'GNOME keyring'],
            ['kwallet', 'KWallet'],
            ['passphrase', 'Passphrase (not recommended)']
        ];
        let repoPlaceHolder = 'Enter the repo used by Borg.';
        if (this.state.mode === 'initNewRepo' && this.state.localRemote === 'remote') {
            repoPlaceHolder = 'Enter the remote path of the repo, such as user@hostname:backup.';
        }
        let repoFieldLength = 10;
        let browseButton = null;
        if (this.state.localRemote === 'local') {
            repoFieldLength = 9;
            browseButton = <FormButton onClick={null}
                                       hint={'Browse local backup directory.'}>Browse</FormButton>
            repoPlaceHolder = 'Enter or browse the local path of the repo home dir used by Borg.';
        }
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
                                     onChange={this.handleRepoTextChange}
                                     placeholder="Enter display name (only for displaying purposes)."/>
                <FormGroup>
                    <FormLabel length={2}>{'Repo'}</FormLabel>
                    <FormField length={repoFieldLength}>
                        <FormInput
                            id={'repo'}
                            name={'repo'}
                            type={'text'}
                            value={repoConfig.repo}
                            onChange={this.handleRepoTextChange}
                            placeholder={repoPlaceHolder}
                        />
                    </FormField>
                    {browseButton}
                </FormGroup>
                <FormLabelInputField label={'RSH'} fieldLength={12}
                                     name={'rsh'} value={repoConfig.rsh}
                                     onChange={this.handleRepoTextChange}
                                     placeholder="Enter the rsh value (ssh command) for remote repository."
                                     className={this.state.localRemote === 'local' ? 'hidden' : null}/>
                <FormGroup className={this.state.mode === 'existingRepo' ? 'hidden' : null}>
                    <FormLabel length={2}>{'Encryption'}</FormLabel>
                    <FormField length={2}>
                        <FormSelect
                            value={repoConfig.encryption}
                            name={'encryption'}
                            onChange={this.handleRepoTextChange}
                            hint={'Encryption for the new repository (use repokey if you don\'t know what to choose).'}
                        >
                            <FormOption label={'repokey (SHA256)'} value={'repokey'}/>
                            <FormOption label={'repokey-blake2'} value={'repokey-blake2'}/>
                            <FormOption label={'keyfile (SHA256)'} value={'keyfile'}/>
                            <FormOption label={'none (not recommended)'} value={'none'}/>
                        </FormSelect>
                    </FormField>
                </FormGroup>
                <FormGroup>
                    <FormLabel length={2}>{'Password method'}</FormLabel>
                    <FormField length={3}>
                        <FormSelect
                            value={this.state.passwordMethod}
                            name={'passwordMethod'}
                            onChange={this.handleTextChange}
                        >
                            {passwordMethods
                                .map((entry) => <FormOption label={entry[1]} value={entry[0]}
                                                            key={entry[0]}/>)}
                        </FormSelect>
                    </FormField>
                </FormGroup>
                <FormGroup className={!this.state.passwordCreate ? 'hidden' : null}>
                    <FormLabel length={2}>{'Passphrase creation info'}</FormLabel>
                    <FormField length={10}>
                        {this.state.passwordCreate}
                    </FormField>
                </FormGroup>
                <FormLabelInputField label={'Password command'} fieldLength={12}
                                     name={'passwordCommand'} value={repoConfig.passwordCommand}
                                     onChange={this.handleRepoTextChange}
                                     placeholder="Enter the password command to get the command from or choose a method above."
                                     className={this.state.passwordMethod === 'passphrase' ? 'hidden' : null}
                />
                <FormLabelInputField label={'Password'} fieldLength={6} type={'password'}
                                     name={'passphrase'} value={repoConfig.passphrase}
                                     onChange={this.handleRepoTextChange}
                                     hint={"It's recommended to use password command instead."}
                                     className={this.state.passwordMethod !== 'passphrase' ? 'hidden' : null}
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
                <h2>Todo:</h2>
                <ul>
                    <li>Implement 'Save' button ;-)</li>
                    <li>Add own environment variables.</li>
                    <li>Implement browse button for local repos.</li>
                    <li>Note (for new backups): Save your password, otherwise your backup will be lost without!</li>
                    <li>Note (hide password fields): Your backup will not be encrypted!</li>
                </ul>
            </code>
        </React.Fragment>;
    }
}

export default ConfigureRepoPage;

