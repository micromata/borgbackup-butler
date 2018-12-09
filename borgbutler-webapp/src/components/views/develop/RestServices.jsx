import React from 'react';
import {PageHeader} from '../../general/BootstrapComponents';
import {getRestServiceUrl, getResponseHeaderFilename} from "../../../utilities/global";
import fileDownload from 'js-file-download';

class RestUrlLink extends React.Component {
    render() {
        const service = this.props.service;
        const params = this.props.params;
        var url;
        if (params) {
            if (service === 'files/browse-local-filesystem') {
                url = getRestServiceUrl(service) + '?' + params;
            } else {
                url = getRestServiceUrl(service) + '?prettyPrinter=true&' + params;
            }
        } else {
            url = getRestServiceUrl(service) + '?prettyPrinter=true';
        }
        return (
            <a href={url}>rest/{service}{params ? '?' + params : ''}</a>
        )
    }
}

class RestServices extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            templateDefinitionId: '',
            templatePrimaryKey: '',
        }
        this.onRun = this.onRun.bind(this);
    }

    componentDidMount() {
        fetch(getRestServiceUrl("templates/example-definitions"), {
            method: "GET",
            dataType: "JSON",
            headers: {
                "Content-Type": "text/plain; charset=utf-8",
            }
        })
            .then((resp) => {
                return resp.json()
            })
            .then((data) => {
                    this.setState({
                        templateDefinitionId: data.templateDefinitionId,
                        templatePrimaryKey: data.templatePrimaryKey
                    });
                }
            )
            .catch((error) => {
                    console.log(error, "Oups, what's happened?")
                }
            )
    }

    onRun() {
        let filename;
        fetch(getRestServiceUrl('templates/example-run-data'), {
            method: "GET",
            dataType: "JSON",
            headers: {
                "Content-Type": "text/plain; charset=utf-8"
            }
        })
            .then((resp) => {
                return resp.json()
            })
            .then((data) => {
                fetch(getRestServiceUrl("templates/run"), {
                    method: 'POST',
                    body: JSON.stringify(data)
                })
                    .then(response => {
                        filename = getResponseHeaderFilename(response.headers.get("Content-Disposition"));
                        //this.setState({downloadFilename: filename});
                        return response.blob();
                    })
                    .then(blob => fileDownload(blob, filename));
            })
            .catch((error) => {
                console.log(error, "Oups, what's happened?")
            })
    }

    render() {
        return (
            <React.Fragment>
                <PageHeader>
                    Rest Services
                </PageHeader>
                <h3>
                    Repositories
                </h3>
                <ul>
                    <li><RestUrlLink service='repos/list'/></li>
                </ul>
                <h3>
                    Config
                </h3>
                <ul>
                    <li><RestUrlLink service='configuration/user'/></li>
                    <li><RestUrlLink service='configuration/config'/></li>
                    <li><RestUrlLink service='version'/> Gets the version and build date of the server.</li>
                    <li><RestUrlLink service='i18n/list'/> Gets all translations. And keys only:{' '}
                        <RestUrlLink service='i18n/list' params={'keysOnly=true'}/> </li>
                </ul>
                <h3>Logging</h3>
                <ul>
                    <li><RestUrlLink service='logging/query'/> (all, default is info log level as treshold)</li>
                    <li><RestUrlLink service='logging/query' params={'treshold=warn'}/> (only warnings)</li>
                    <li><RestUrlLink service='logging/query' params={'treshold=info&search=server'}/> (search for server)</li>
                </ul>
            </React.Fragment>
        );
    }
}

export default RestServices;
