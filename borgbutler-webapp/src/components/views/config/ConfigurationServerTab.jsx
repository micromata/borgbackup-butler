import React from 'react';
import {Alert} from 'reactstrap';
import {
    FormCheckbox,
    FormField,
    FormGroup,
    FormInput,
    FormLabel,
    FormLabelField,
    FormLabelInputField,
    FormOption,
    FormSelect
} from '../../general/forms/FormComponents';
import {getRestServiceUrl} from '../../../utilities/global';
import I18n from '../../general/translation/I18n';
import ErrorAlertGenericRestFailure from '../../general/ErrorAlertGenericRestFailure';
import Loading from '../../general/Loading';

class ConfigServerTab extends React.Component {
    loadConfig = () => {
        this.setState({
            loading: true,
            failed: false
        });
        fetch(getRestServiceUrl('configuration/config'), {
            method: 'GET',
            dataType: 'JSON',
            headers: {
                'Content-Type': 'text/plain; charset=utf-8'
            }
        })
            .then((resp) => {
                return resp.json()
            })
            .then((data) => {
                this.setState({
                    loading: false,
                    borgBinary: data.borgVersion.borgBinary,
                    borgCommand: data.borgVersion.borgCommand,
                    ...data
                })
            })
            .catch((error) => {
                console.log("error", error);
                this.setState({
                    loading: false,
                    failed: true
                });
            })
    };

    constructor(props) {
        super(props);

        this.state = {
            loading: true,
            failed: false,
            port: 9042,
            webdevelopmentMode: false,
            showDemoRepos: true,
            maxArchiveContentCacheCapacityMb: 100,
            redirect: false,
            borgCommand: null,
            borgBinary: null
        };

        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.loadConfig = this.loadConfig.bind(this);
    }

    componentDidMount() {
        this.loadConfig();
    }

    handleTextChange = event => {
        event.preventDefault();
        this.setState({[event.target.name]: event.target.value});
    }

    handleCheckboxChange = event => {
        this.setState({[event.target.name]: event.target.checked});
    }

    save() {
        var config = {
            port: this.state.port,
            maxArchiveContentCacheCapacityMb: this.state.maxArchiveContentCacheCapacityMb,
            webDevelopmentMode: this.state.webDevelopmentMode,
            showDemoRepos: this.state.showDemoRepos,
            borgVersion: {
                borgCommand: this.state.borgCommand,
                borgBinary: this.state.borgBinary
            }
        };
        return fetch(getRestServiceUrl("configuration/config"), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        })
    }

    static clearAllCaches() {
        fetch(getRestServiceUrl('configuration/clearAllCaches'), {
            method: 'GET',
            dataType: 'JSON',
            headers: {
                'Content-Type': 'text/plain; charset=utf-8'
            }
        })
    }

    render() {
        if (this.state.loading) {
            return <Loading/>;
        }

        if (this.state.failed) {
            return <ErrorAlertGenericRestFailure handleClick={this.loadConfig}/>;
        }
        const borgVersion = this.state.borgVersion;
        let borgInfoColor = 'success';
        let borgInfoMessage = `Borg version '${borgVersion.version}' is OK.`;
        if (!borgVersion.versionOK) {
            borgInfoColor = 'danger';
            borgInfoMessage = borgVersion.statusMessage;
        }
        return (
            <React.Fragment>
                <form>
                    <FormGroup>
                        <FormLabel>{'Borg command'}</FormLabel>
                        <FormField length={2}>
                            <FormSelect
                                value={this.state.borgBinary}
                                name={'borgBinary'}
                                onChange={this.handleTextChange}
                                hint={`Choose your OS and BorgButler will download and use a ready to run borg binary from ${borgVersion.binariesDownloadUrl} or choose a manual installed version.`}
                            >
                                {borgVersion.borgBinaries
                                    .map((binary, index) => <FormOption label={binary[1]} value={binary[0]}
                                                                        key={index}/>)}
                                <FormOption label={'Manual'} value={'manual'}/>
                            </FormSelect>
                        </FormField>
                        <FormInput fieldLength={8} name={'borgCommand'} value={this.state.borgCommand}
                                   onChange={this.handleTextChange}
                                   placeholder="Enter path of borg command"
                                   disabled={this.state.borgBinary !== "manual"}/>
                    </FormGroup>
                    <FormGroup>
                        <FormField length={4}/>
                        <Alert className={'col-sm-8'} color={borgInfoColor}>
                            {borgInfoMessage}
                        </Alert>
                    </FormGroup>
                    <FormLabelInputField label={'Port'} fieldLength={2} type="number" min={0} max={65535}
                                         step={1}
                                         name={'port'} value={this.state.port}
                                         onChange={this.handleTextChange}
                                         placeholder="Enter port"/>
                    <FormLabelInputField label={'Maximum disc capacity (MB)'} fieldLength={2} type="number" min={50}
                                         max={10000}
                                         step={50}
                                         name={'maxArchiveContentCacheCapacityMb'}
                                         value={this.state.maxArchiveContentCacheCapacityMb}
                                         onChange={this.handleTextChange}
                                         placeholder="Enter maximum Capacity"
                                         hint={`Limits the cache size of archive file lists in the local cache directory: ${this.state.cacheDir}`}/>
                    <FormLabelField label={'Show demo repositories'} fieldLength={2}>
                        <FormCheckbox checked={this.state.showDemoRepos}
                                      hint={'If true, some demo repositories are shown for testing the functionality of BorgButler without any further configuration and running borg backups.'}
                                      name="showDemoRepos"
                                      onChange={this.handleCheckboxChange}/>
                    </FormLabelField>
                    <FormLabelField label={<I18n name={'configuration.webDevelopmentMode'}/>} fieldLength={2}>
                        <FormCheckbox checked={this.state.webDevelopmentMode}
                                      hintKey={'configuration.webDevelopmentMode.hint'}
                                      name="webDevelopmentMode"
                                      onChange={this.handleCheckboxChange}/>
                    </FormLabelField>
                </form>
            </React.Fragment>
        );
    }
}

export default ConfigServerTab;

