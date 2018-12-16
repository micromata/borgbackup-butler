import React from 'react';
import { Button, Modal, ModalHeader, ModalBody, ModalFooter } from 'reactstrap';

class ConfirmReloadDialog extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            modal: false
        };

        this.toggle = this.toggle.bind(this);
    }

    toggle() {
        this.setState({
            modal: !this.state.modal
        });
    }

    render() {
        return (
            <div>
                <Button color="danger" onClick={this.toggle}>{this.props.buttonLabel}</Button>
                <Modal isOpen={this.state.modal} toggle={this.toggle} className={this.props.className}>
                    <ModalHeader toggle={this.toggle}>Do you really want to reload?</ModalHeader>
                    <ModalBody>
                        Reloading of the data is time consuming for remote borg repos. Reloading is only required
                        if you assume that the cache data of BorgButler is outdated.
                    </ModalBody>
                    <ModalFooter>
                        <Button color="secondary" onClick={this.toggle}>Cancel</Button>
                        <Button color="primary" onClick={this.toggle}>Reload</Button>{' '}
                    </ModalFooter>
                </Modal>
            </div>
        );
    }
}

export default ConfirmReloadDialog;