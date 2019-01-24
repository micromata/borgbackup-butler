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
import ConfirmModal from '../../general/modal/ConfirmModal';

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
            reload: false,
            confirmModal: false
        };

        this.onSave = this.onSave.bind(this);
        this.onCancel = this.onCancel.bind(this);
        this.toggleModal = this.toggleModal.bind(this);
    }

    toggleTab = tab => () => {
        this.setState({
            activeTab: tab
        })
    };

    // TODO: Don't reload, display toast instead.
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

    toggleModal() {
        this.setState({
            confirmModal: !this.state.confirmModal
        })
    }

    render() {
        // https://codepen.io/_arpy/pen/xYoyPW
        if (this.state.reload) {
            window.location.reload();
        }
        console.log('TODO: render called (should only called after state was changed, shouldn\'t?'); // TODO
        return (
            <React.Fragment>
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
                        <FormButton id={'clearAllCaches'} onClick={this.toggleModal}> Clear all caches
                        </FormButton>
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

