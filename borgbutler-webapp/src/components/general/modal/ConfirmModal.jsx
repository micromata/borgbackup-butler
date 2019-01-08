import React from 'react';
import * as PropTypes from 'prop-types';
import {Button, Modal, ModalBody, ModalFooter, ModalHeader} from 'reactstrap';

class ConfirmModal extends React.Component {

    constructor(props) {
        super(props);

        this.confirm = this.confirm.bind(this);
    }

    confirm() {
        this.props.toggle();
        this.props.onConfirm();
    }

    render() {
        return (
            <Modal isOpen={this.props.open} toggle={this.props.toggle}>
                <ModalHeader toggle={this.props.toggle}>{this.props.title}</ModalHeader>
                <ModalBody>
                    {this.props.children}
                </ModalBody>
                <ModalFooter>
                    <Button color={'secondary'} onClick={this.props.toggle}>{this.props.cancelButton}</Button>
                    <Button color={'primary'} onClick={this.confirm}>{this.props.confirmButton}</Button>
                </ModalFooter>
            </Modal>
        );
    }
}

ConfirmModal.propTypes = {
    onConfirm: PropTypes.func.isRequired,
    title: PropTypes.string.isRequired,
    children: PropTypes.node.isRequired,
    toggle: PropTypes.func.isRequired,
    open: PropTypes.bool,
    confirmButton: PropTypes.string,
    cancelButton: PropTypes.string
};

ConfirmModal.defaultProps = {
    open: false,
    confirmButton: 'Ok',
    cancelButton: 'Cancel'
};

export default ConfirmModal;
