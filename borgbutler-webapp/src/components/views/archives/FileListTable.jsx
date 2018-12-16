import React from 'react';
import PropTypes from 'prop-types';
import {Table} from 'reactstrap';
import FileListEntry from './FileListEntry';

function FileListTable({entries, search}) {
    const lowercaseSearch = search.toLowerCase();
    return (
        <Table striped bordered hover size={'sm'} responsive>
            <thead>
            <tr>
                <th>Mode</th>
                <th style={{whiteSpace: 'nowrap'}}>
                    Date
                </th>
                <th>Path</th>
            </tr>
            </thead>
            <tbody>
            {entries
                .filter(entry => [entry.message]
                    .join('|#|').toLowerCase()
                    .indexOf(lowercaseSearch) !== -1)
                .map((entry, index) => <FileListEntry
                    entry={entry}
                    search={lowercaseSearch}
                    key={index}
                />)}
            </tbody>
        </Table>
    );
}

FileListTable.propTypes = {
    entries: PropTypes.array,
    search: PropTypes.string
};

FileListTable.defaultProps = {
    entries: [],
    search: ''
};

export default FileListTable;
