import React from 'react';
import {FormButton, FormCheckbox, FormLabelField, FormLabelInputField} from "../../general/forms/FormComponents";
import {getRestServiceUrl, isDevelopmentMode} from "../../../utilities/global";
import I18n from "../../general/translation/I18n";
import ErrorAlertGenericRestFailure from '../../general/ErrorAlertGenericRestFailure';
import Loading from "../../general/Loading";

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
            maxArchiveContentCacheCapacityMb: 100,
            redirect: false
        };

        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.onClearAllCaches = this.onClearAllCaches.bind(this);
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
            maxArchiveContentCacheCapacityMb : this.state.maxArchiveContentCacheCapacityMb,
            webDevelopmentMode: this.state.webDevelopmentMode
        };
        return fetch(getRestServiceUrl("configuration/config"), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        })
    }

    onClearAllCaches() {
        if (window.confirm('Do you really want to clear all caches? All Archive file lists and caches for repo and archive informatino will be cleared.')) {
            fetch(getRestServiceUrl("configuration/clearAllCaches"), {
                method: "GET",
                dataType: "JSON",
                headers: {
                    "Content-Type": "text/plain; charset=utf-8"
                }
            })
        }
    }

    render() {
        let todo = '';
        if (this.state.loading) {
            return <Loading/>;
        }

        if (this.state.failed) {
            return <ErrorAlertGenericRestFailure handleClick={this.loadConfig}/>;
        }
        if (isDevelopmentMode()) {
            todo = <code>
                    <h2>To-do</h2>
                    Statt dem windows.confirm-Dialog nach Klicken auf 'Clear all caches' einen Modaldialog nehmen (in der Art
                    von ConfirmReloadDialog.jsx). Am besten als Komponente verwendbar.
                </code>
        }

        return (
            <form>
                <FormLabelField>
                    <FormButton id={'clearAllCaches'} onClick={this.onClearAllCaches}> Clear all caches
                    </FormButton>
                </FormLabelField>
                <FormLabelInputField label={'Port'} fieldLength={2} type="number" min={0} max={65535}
                                     step={1}
                                     name={'port'} value={this.state.port}
                                     onChange={this.handleTextChange}
                                     placeholder="Enter port"/>
                <FormLabelInputField label={'Maximum disc capacity (MB)'} fieldLength={2} type="number" min={50} max={10000}
                                     step={50}
                                     name={'maxArchiveContentCacheCapacityMb'} value={this.state.maxArchiveContentCacheCapacityMb}
                                     onChange={this.handleTextChange}
                                     placeholder="Enter maximum Capacity"
                hint={`Limits the cache size of archive file lists in the local cache directory: ${this.state.cacheDir}`}/>
                <FormLabelField label={<I18n name={'configuration.webDevelopmentMode'}/>} fieldLength={2}>
                    <FormCheckbox checked={this.state.webDevelopmentMode}
                                  hintKey={'configuration.webDevelopmentMode.hint'}
                                  name="webDevelopmentMode"
                                  onChange={this.handleCheckboxChange}/>
                </FormLabelField>
                {todo}
            </form>
        );
    }
}

export default ConfigServerTab;

