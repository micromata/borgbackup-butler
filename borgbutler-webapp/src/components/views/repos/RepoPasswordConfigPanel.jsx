import React from 'react';
import {Alert} from 'reactstrap';
import {
    FormField,
    FormGroup,
    FormLabel,
    FormLabelInputField,
    FormOption,
    FormSelect
} from '../../general/forms/FormComponents';
import PropTypes from "prop-types";

class RepoPasswordConfigPanel extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            passwordMethod: 'passwordCommand',
            passwordCreate: null
        };
        this.handleTextChange = this.handleTextChange.bind(this);
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
                passwordCommand = 'security find-generic-password -a $USER -s borg-passphrase -w';
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
                // TODO
                this.setState({repoConfig: {...this.state.repoConfig, 'passwordCommand': passwordCommand}})
            }
            this.setState({'passwordCreate': passwordCreate});
        }
    }


    render() {
        var passwordMethods = [['password-command', 'Password command'],
            ['passwordFile', 'Password file'],
            ['macos-keychain', 'Mac OS X keychain'],
            ['gnome-keyring', 'GNOME keyring'],
            ['kwallet', 'KWallet'],
            ['passphrase', 'Passphrase (not recommended)'],
            ['none', 'No password (no encryption, not recommended)']
        ];
        let encrypted = true;
        if (this.props.encryption === 'none' || this.state.passwordMethod === 'none') {
            encrypted = false;
        }
        return <React.Fragment>
            <FormGroup
                className={this.props.encryption === 'none' ? 'hidden' : null}
            >
                <FormLabel length={2}>{'Password method'}</FormLabel>
                <FormField length={4}>
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
                                 name={'passwordCommand'} value={this.props.repoConfig.passwordCommand}
                                 onChange={this.props.handleRepoConfigChange}
                                 placeholder="Enter the password command to get the command from or choose a method above."
                                 className={!encrypted || this.state.passwordMethod === 'passphrase' ? 'hidden' : null}
            />
            <FormLabelInputField label={'Password'} fieldLength={6} type={'password'}
                                 name={'passphrase'} value={this.props.repoConfig.passphrase}
                                 onChange={this.props.handleRepoConfigChange}
                                 hint={"It's recommended to use password command instead."}
                                 className={(!encrypted || this.state.passwordMethod !== 'passphrase') ? 'hidden' : null}
            />
            <FormGroup className={!encrypted ? 'hidden' : null}>
                <FormField length={2}>
                </FormField>
                <FormField length={10}>
                    <Alert
                        color={'warning'}
                    >
                        Please keep a copy of your password safe! If your password get lost, your backup might be
                        lost!
                    </Alert>
                </FormField>
            </FormGroup>
            <FormGroup className={encrypted ? 'hidden' : null}>
                <FormField length={2}>
                </FormField>
                <FormField length={10}>
                    <Alert
                        color={'danger'}
                    >
                        You backup isn't encrpyted! You should ensure, that your destination storage is encrypted
                        and protected.
                    </Alert>
                </FormField>
            </FormGroup>
        </React.Fragment>;
    }
}

RepoPasswordConfigPanel.propTypes = {
    handleRepoConfigChange: PropTypes.func.isRequired,
    repoConfig: PropTypes.object.isRequired,
    encryption: PropTypes.string
};

RepoPasswordConfigPanel.defaultProps = {
    editExistingRepo: 'none'
};

export default RepoPasswordConfigPanel;
