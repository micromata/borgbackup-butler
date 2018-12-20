import React from 'react';
import {FormButton, FormCheckbox, FormLabelField, FormLabelInputField} from "../../general/forms/FormComponents";
import {getRestServiceUrl} from "../../../utilities/global";
import {IconDanger} from '../../general/IconComponents';
import {getTranslation} from "../../../utilities/i18n";
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
            webDevelopmentMode: false,
            redirect: false
        };

        this.handleTextChange = this.handleTextChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.onResetConfiguration = this.onResetConfiguration.bind(this);
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

    onResetConfiguration() {
        if (window.confirm(getTranslation('configuration.resetAllSettings.question'))) {
            fetch(getRestServiceUrl("configuration/reset?IKnowWhatImDoing=true"), {
                method: "GET",
                dataType: "JSON",
                headers: {
                    "Content-Type": "text/plain; charset=utf-8"
                }
            })
        }
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
        if (this.state.loading) {
            return <Loading/>;
        }

        if (this.state.failed) {
            return <ErrorAlertGenericRestFailure handleClick={this.loadConfig}/>;
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
                <FormLabelField label={<I18n name={'configuration.webDevelopmentMode'}/>} fieldLength={2}>
                    <FormCheckbox checked={this.state.webDevelopmentMode}
                                  hintKey={'configuration.webDevelopmentMode.hint'}
                                  name="webDevelopmentMode"
                                  onChange={this.handleCheckboxChange}/>
                </FormLabelField>
                <FormLabelField>
                    <FormButton id={'resetFactorySettings'} onClick={this.onResetConfiguration}
                                hintKey={'configuration.resetAllSettings.hint'}> <IconDanger/> <I18n
                        name={'configuration.resetAllSettings'}/>
                    </FormButton>
                </FormLabelField>
            </form>
        );
    }
}

export default ConfigServerTab;

