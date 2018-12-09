import React from 'react';
import {PageHeader} from '../general/BootstrapComponents';
import I18n from "../general/translation/I18n";

class Start extends React.Component {
    render() {
        return (
            <React.Fragment>
                <PageHeader>
                    Welcome to BorgButler
                </PageHeader>
                <div className="welcome-intro">BorgButler is the frontend for BorgBackup.</div>
                <div className="welcome-enjoy">Enjoy your work with BorgButler.</div>
                <div className="welcome-documentation-link"><a className={'btn btn-link btn-outline-primary'} href={'https://github.com/micromata/borgbackup-butler'} target="_blank" rel="noopener noreferrer">Documentation</a></div>
            </React.Fragment>
        );
    }
}

export default Start;
