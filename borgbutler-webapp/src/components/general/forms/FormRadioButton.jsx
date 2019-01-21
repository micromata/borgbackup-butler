import React from 'react';
import PropTypes from 'prop-types';
import {UncontrolledTooltip} from 'reactstrap';
import {revisedRandId} from "../../../utilities/global";
import classNames from 'classnames';
import I18n from "../translation/I18n";

class FormRadioButton extends React.Component {

    _id = this.props.id || revisedRandId();

    render() {
        const {id, className, labelKey, label, hint, hintKey, ...other} = this.props;
        let tooltip = null;
        let hintId = null;
        if (hint || hintKey) {
            hintId = `hint-${this._id}`;
            tooltip =
                <UncontrolledTooltip placement="right" target={hintId}>
                    {hintKey ? <I18n name={hintKey}/> : hint}
                </UncontrolledTooltip>;
        }
        let labelNode = <label
            className={'custom-control-label'}
            htmlFor={this._id}>
            {labelKey ? <I18n name={labelKey}/> : this.props.label}
        </label>;
        return (
            <React.Fragment>
                <div className="custom-control custom-radio custom-control-inline" id={hintId}>
                    <input type="radio"
                           id={this._id}
                           className={classNames('custom-control-input', className)}
                           {...other}
                    />
                    {labelNode}
                </div>
                {tooltip}
            </React.Fragment>
        );
    }
}

FormRadioButton.propTypes = {
    id: PropTypes.string,
    name: PropTypes.string,
    checked: PropTypes.bool,
    onChange: PropTypes.func,
    hint: PropTypes.string,
    label: PropTypes.node,
    labelKey: PropTypes.string
};

FormRadioButton.defaultProps = {
    checked: false,
    onChange: null
};


export {
    FormRadioButton
};
