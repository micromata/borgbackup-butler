import React from 'react';
import {
    faBan,
    faCaretDown,
    faCaretUp,
    faCheck,
    faCircleNotch,
    faDownload,
    faExclamationTriangle,
    faInfoCircle,
    faPlus,
    faSkullCrossbones,
    faSortDown,
    faSortUp,
    faSync,
    faTimes,
    faTrash,
    faUpload
} from '@fortawesome/free-solid-svg-icons'
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

function IconAdd() {
    return (
        <FontAwesomeIcon icon={faPlus}/>
    );
}

function IconBan() {
    return (
        <FontAwesomeIcon icon={faBan}/>
    );
}

function IconCancel() {
    return (
        <FontAwesomeIcon icon={faTimes}/>
    );
}

function IconCheck() {
    return (
        <FontAwesomeIcon icon={faCheck}/>
    );
}

function IconCollapseClose() {
    return (
        <FontAwesomeIcon icon={faCaretUp}/>
    );
}

function IconCollapseOpen() {
    return (
        <FontAwesomeIcon icon={faCaretDown}/>
    );
}

function IconDanger() {
    return (
        <FontAwesomeIcon icon={faSkullCrossbones}/>
    );
}

function IconDownload() {
    return (
        <FontAwesomeIcon icon={faDownload}/>
    );
}

function IconInfo() {
    return (
        <FontAwesomeIcon icon={faInfoCircle}/>
    );
}

function IconRefresh() {
    return (
        <FontAwesomeIcon icon={faSync}/>
    );
}

function IconRemove() {
    return (
        <FontAwesomeIcon icon={faTrash}/>
    );
}

function IconSpinner() {
    return (
        <FontAwesomeIcon icon={faCircleNotch} spin={true} size={'3x'} color={'#aaaaaa'}/>
    );
}

function IconSortDown() {
    return (
        <FontAwesomeIcon icon={faSortDown}/>
    );
}

function IconSortUp() {
    return (
        <FontAwesomeIcon icon={faSortUp}/>
    );
}

function IconUpload() {
    return (
        <FontAwesomeIcon icon={faUpload}/>
    );
}

function IconWarning() {
    return (
        <FontAwesomeIcon icon={faExclamationTriangle}/>
    );
}

export {
    IconAdd,
    IconBan,
    IconCancel,
    IconCheck,
    IconCollapseClose,
    IconCollapseOpen,
    IconDanger,
    IconDownload,
    IconInfo,
    IconRefresh,
    IconRemove,
    IconSortDown,
    IconSortUp,
    IconSpinner,
    IconUpload,
    IconWarning
};
