import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import {UncontrolledTooltip} from 'reactstrap';
import {getTranslation} from '../../../utilities/i18n';

const FormSelect = (props) => {
    let tooltip = null;
    let targetId = props.id || props.name;
    if (props.hint) {
        tooltip = <UncontrolledTooltip placement={props.hintPlacement} target={targetId}>
            {props.hint}
        </UncontrolledTooltip>;
    }
    const {fieldLength, className, hint, hintPlacement, id, ...other} = props;
    let myClassName = className;
    if (fieldLength > 0) {
        myClassName = classNames(`col-sm-${props.fieldLength}`, className);
    }
    return (
        <React.Fragment>
            <select id={targetId}
                    className={classNames('custom-select form-control form-control-sm mr-1', myClassName)}
                    {...other}
            >
                {props.children}
            </select>
            {tooltip}
        </React.Fragment>
    );
}

FormSelect.propTypes = {
    id: PropTypes.string,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number, PropTypes.bool]),
    name: PropTypes.string,
    onChange: PropTypes.func,
    fieldLength: PropTypes.number,
    hint: PropTypes.string,
    hintPlacement: PropTypes.oneOf(['right', 'top']),
    children: PropTypes.node,
    className: PropTypes.string
};

FormSelect.defaultProps = {
    hint: null,
    hintPlacement: 'top',
};


const FormOption = (props) => {
    let label;
    if (props.i18nKey) {
        label = getTranslation(props.i18nKey) || props.value;
    } else {
        label = props.label || props.value;
    }
    const {value, i18nKey, ...other} = props;
    return (
        <React.Fragment>
            <option value={props.value}
                    {...other}
            >
                {label}
            </option>
        </React.Fragment>
    );
}

FormSelect.propTypes = {
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number, PropTypes.bool]),
    i18nKey: PropTypes.string,
    label: PropTypes.string,
    disabled: PropTypes.bool
};

export {
    FormSelect, FormOption
};
