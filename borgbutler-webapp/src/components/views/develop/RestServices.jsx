import React from 'react';
import {PageHeader} from '../../general/BootstrapComponents';
import {getRestServiceUrl} from "../../../utilities/global";

class RestUrlLink extends React.Component {
    render() {
        const service = this.props.service;
        const params = this.props.params;
        var url;
        if (params) {
            url = getRestServiceUrl(service) + '?' + params;
        } else {
            url = getRestServiceUrl(service);
        }
        return (
            <a href={url}>rest/{service}{params ? '?' + params : ''}</a>
        )
    }
}

class RestServices extends React.Component {
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
                    Job monitor
                </h3>
                <ul>
                    <li><RestUrlLink service='jobs'/></li>
                </ul>
                <h3>
                    Config
                </h3>
                <ul>
                    <li><RestUrlLink service='configuration/user'/></li>
                    <li><RestUrlLink service='configuration/config'/></li>
                    <li><RestUrlLink service='version'/> Gets the version and build date of the server.</li>
                    <li><RestUrlLink service='i18n/list'/> Gets all translations. And keys only:{' '}
                        <RestUrlLink service='i18n/list' params={'keysOnly=true'}/></li>
                </ul>
                <h3>Logging</h3>
                <ul>
                    <li><RestUrlLink service='logging/query'/> (all, default is info log level as treshold)</li>
                    <li><RestUrlLink service='logging/query' params={'treshold=warn'}/> (only warnings)</li>
                    <li><RestUrlLink service='logging/query' params={'treshold=info&search=server'}/> (search for
                        server)
                    </li>
                </ul>
            </React.Fragment>
        );
    }
}

export default RestServices;
