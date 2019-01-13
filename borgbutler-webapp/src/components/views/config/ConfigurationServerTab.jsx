import React from 'react';
import {FormButton, FormCheckbox, FormLabelField, FormLabelInputField} from '../../general/forms/FormComponents';
import {getRestServiceUrl} from '../../../utilities/global';
import I18n from '../../general/translation/I18n';
import ErrorAlertGenericRestFailure from '../../general/ErrorAlertGenericRestFailure';
import Loading from '../../general/Loading';
import ConfirmModal from '../../general/modal/ConfirmModal';

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
            confirmModal: false
        };

        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.loadConfig = this.loadConfig.bind(this);
        this.toggleModal = this.toggleModal.bind(this);
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
            maxArchiveContentCacheCapacityMb : this.state.maxArchiveContentCacheCapacityMb,
            webDevelopmentMode: this.state.webDevelopmentMode,
            showDemoRepos: this.state.showDemoRepos
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

    toggleModal() {
        this.setState({
            confirmModal: !this.state.confirmModal
        })
    }

    render() {
        if (this.state.loading) {
            return <Loading/>;
        }

        if (this.state.failed) {
            return <ErrorAlertGenericRestFailure handleClick={this.loadConfig}/>;
        }

        return (
            <div>
                <ConfirmModal
                    onConfirm={ConfigServerTab.clearAllCaches}
                    title={'Are you sure?'}
                    toggle={this.toggleModal}
                    open={this.state.confirmModal}
                >
                    Do you really want to clear all caches? All Archive file lists and caches for repo and archive
                    information will be cleared.
                    <br/>
                    This is a safe option but it may take some time to re-fill the caches (on demand) again.
                </ConfirmModal>
                <form>
                    <FormLabelField>
                        <FormButton id={'clearAllCaches'} onClick={this.toggleModal}> Clear all caches
                        </FormButton>
                    </FormLabelField>
                    <FormLabelInputField label={'Port'} fieldLength={2} type="number" min={0} max={65535}
                                         step={1}
                                         name={'port'} value={this.state.port}
                                         onChange={this.handleTextChange}
                                         placeholder="Enter port" />
                    <FormLabelInputField label={'Maximum disc capacity (MB)'} fieldLength={2} type="number" min={50}
                                         max={10000}
                                         step={50}
                                         name={'maxArchiveContentCacheCapacityMb'}
                                         value={this.state.maxArchiveContentCacheCapacityMb}
                                         onChange={this.handleTextChange}
                                         placeholder="Enter maximum Capacity"
                                         hint={`Limits the cache size of archive file lists in the local cache directory: ${this.state.cacheDir}`} />
                    <FormLabelField label={'Show demo repositories'} fieldLength={2}>
                        <FormCheckbox checked={this.state.showDemoRepos}
                                      hint={'If true, some demo repositories are shown for testing the functionality of BorgButler without any further configuration and running borg backups.'}
                                      name="showDemoRepos"
                                      onChange={this.handleCheckboxChange} />
                    </FormLabelField>
                    <FormLabelField label={<I18n name={'configuration.webDevelopmentMode'} />} fieldLength={2}>
                        <FormCheckbox checked={this.state.webDevelopmentMode}
                                      hintKey={'configuration.webDevelopmentMode.hint'}
                                      name="webDevelopmentMode"
                                      onChange={this.handleCheckboxChange} />
                    </FormLabelField>
                </form>
            </div>
        );
    }
}

export default ConfigServerTab;

