import React from 'react';
import PropTypes from 'prop-types';
import {Table} from 'reactstrap';
import FileListEntry from './FileListEntry';

function FileListTable({archive, diffArchiveId, openDownloads, entries, search, mode, changeCurrentDirectory}) {
    const lowercaseSearch = search.split(' ')[0].toLowerCase();
    return (
        <Table striped bordered hover size={'sm'} responsive>
            <thead>
            <tr>
                <th>Path</th>
                <th></th>
                <th>Size</th>
                <th>Mode</th>
                <th>Modified time</th>
            </tr>
            </thead>
            <tbody>
            {entries
                .map((entry, index) => <FileListEntry
                    archive={archive}
                    diffArchiveId={diffArchiveId}
                    entry={entry}
                    search={lowercaseSearch}
                    mode={mode}
                    openDownloads={openDownloads}
                    key={index}
                />)}
            </tbody>
        </Table>
    );
}

FileListTable.propTypes = {
    diffArchiveId: PropTypes.string,
    entries: PropTypes.array,
    search: PropTypes.string,
    mode: PropTypes.string
};

FileListTable.defaultProps = {
    entries: [],
    search: '',
    mode: 'flat'
};

export default FileListTable;
