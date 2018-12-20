import React from 'react';
import PropTypes from 'prop-types';
import {Table} from 'reactstrap';
import FileListEntry from './FileListEntry';

function FileListTable({archiveId, diffArchiveId, entries, search, mode, changeCurrentDirectory}) {
    const lowercaseSearch = search.split(' ')[0].toLowerCase();
    let diffCol = undefined;
    if (diffArchiveId) {
        diffCol = <th>Modification</th>;
    }
    return (
        <Table striped bordered hover size={'sm'} responsive>
            <thead>
            <tr>
                <th>Path</th>
                <th></th>
                <th>Size</th>
                <th>Mode</th>
                <th>Modified time</th>
                {diffCol}
            </tr>
            </thead>
            <tbody>
            {entries
                .map((entry, index) => <FileListEntry
                    archiveId={archiveId}
                    diffArchiveId={diffArchiveId}
                    entry={entry}
                    search={lowercaseSearch}
                    mode={mode}
                    changeCurrentDirectory={changeCurrentDirectory}
                    key={index}
                />)}
            </tbody>
        </Table>
    );
}

FileListTable.propTypes = {
    archiveId: PropTypes.string,
    diffArchiveId: PropTypes.string,
    entries: PropTypes.array,
    search: PropTypes.string,
    mode: PropTypes.string,
    changeCurrentDirectory: PropTypes.func.isRequired
};

FileListTable.defaultProps = {
    entries: [],
    search: '',
    mode: 'flat'
};

export default FileListTable;
