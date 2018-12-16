import React from 'react';
import PropTypes from 'prop-types';
import {Table} from 'reactstrap';
import FileListEntry from './FileListEntry';

function FileListTable({archiveId, entries, search}) {
    const lowercaseSearch = search.split(' ')[0].toLowerCase();
    return (
        <Table striped bordered hover size={'sm'} responsive>
            <thead>
            <tr>
                <th>Mode</th>
                <th>Modified time</th>
                <th>Size</th>
                <th></th>
                <th>Path</th>
            </tr>
            </thead>
            <tbody>
            {entries
                .map((entry, index) => <FileListEntry
                    archiveId={archiveId}
                    entry={entry}
                    search={lowercaseSearch}
                    key={index}
                />)}
            </tbody>
        </Table>
    );
}

FileListTable.propTypes = {
    archiveId: PropTypes.string,
    entries: PropTypes.array,
    search: PropTypes.string
};

FileListTable.defaultProps = {
    entries: [],
    search: ''
};

export default FileListTable;
