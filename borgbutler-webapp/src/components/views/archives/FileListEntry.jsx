import React from 'react';
import PropTypes from 'prop-types';
import Highlight from 'react-highlighter';

function FileListEntry({entry, search}) {
    return (
        <tr>
            <td className={'tt'}>{entry.mode}</td>
            <td className={'tt'}><Highlight search={search}>{entry.path}</Highlight></td>
        </tr>
    );
}

FileListEntry.propTypes = {
    entry: PropTypes.shape({}).isRequired,
    search: PropTypes.string,
};

export default FileListEntry;