import React from 'react';
import {FormGroup, Nav, NavLink, TabContent, TabPane} from 'reactstrap';
import {PageHeader} from '../../general/BootstrapComponents';
import {FormButton, FormField} from '../../general/forms/FormComponents';
import {clearDictionary} from '../../../utilities/i18n';
import I18n from "../../general/translation/I18n";
import classNames from "classnames";
import ConfigAccountTab from "./ConfigurationAccountTab";
import ConfigServerTab from "./ConfigurationServerTab";
import LoadingOverlay from '../../general/loading/LoadingOverlay';

class ConfigurationPage
    extends React
        .Component {

    constructor(props) {
        super(props);
        this.serverTabRef = React.createRef();
        this.accountTabRef = React.createRef();
        this.state = {
            activeTab: '1',
            loading: false,
            reload: false
        };

        this.onSave = this.onSave.bind(this);
        this.onCancel = this.onCancel.bind(this);
    }

    toggleTab = tab => () => {
        this.setState({
            activeTab: tab
        })
    };

    setReload = () => {
        this.setState({
            reload: true
        })
    }

    async onSave(event) {
        this.setState({
            loading: true
        })
        const cb1 = this.serverTabRef.current ? this.serverTabRef.current.save() : null;
        const cb2 = this.accountTabRef.current ? this.accountTabRef.current.save() : null;
        if (cb1) await cb1;
        if (cb2) await cb2;
        clearDictionary();
        this.setState({
            loading: false
        })
        this.setReload();
    }

    onCancel() {
        this.setReload();
    }

    render() {
        // https://codepen.io/_arpy/pen/xYoyPW
        if (this.state.reload) {
            window.location.reload();
        }
        return (
            <React.Fragment>
                <PageHeader><I18n name={'configuration'}/></PageHeader>
                <Nav tabs>
                    <NavLink
                        className={classNames({active: this.state.activeTab === '1'})}
                        onClick={this.toggleTab('1')}
                    >
                        <I18n name={'configuration.server'}/>
                    </NavLink>
                    <NavLink
                        className={classNames({active: this.state.activeTab === '2'})}
                        onClick={this.toggleTab('2')}
                    >
                        <I18n name={'configuration.account'}/>
                    </NavLink>
                </Nav>
                <TabContent activeTab={this.state.activeTab}>
                    <TabPane tabId={'1'}>
                        <ConfigServerTab ref={this.serverTabRef}/>
                    </TabPane>
                </TabContent>
                <TabContent activeTab={this.state.activeTab}>
                    <TabPane tabId={'2'}>
                        <ConfigAccountTab ref={this.accountTabRef}/>
                    </TabPane>
                </TabContent>
                <FormGroup>
                    <FormField length={12}>
                        <FormButton onClick={this.onCancel}
                                    hintKey="configuration.cancel.hint"><I18n name={'common.cancel'}/>
                        </FormButton>
                        <FormButton onClick={this.onSave} bsStyle="primary"
                                    hintKey="configuration.save.hint"><I18n name={'common.save'}/>
                        </FormButton>
                    </FormField>
                </FormGroup>
                <LoadingOverlay active={this.state.loading} />
            </React.Fragment>
        );
    }
}

export default ConfigurationPage;

