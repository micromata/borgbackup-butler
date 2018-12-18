import React from 'react';
import PropTypes from 'prop-types';
import Highlight from 'react-highlighter';
import {IconDownload} from '../../general/IconComponents';
import {getResponseHeaderFilename, getRestServiceUrl, humanFileSize} from '../../../utilities/global';
import fileDownload from 'js-file-download';
import {Button} from 'reactstrap';

function download(archiveId, fileNumber) {
    let filename;
    fetch(getRestServiceUrl('archives/restore', {
        archiveId: archiveId,
        fileNumber: fileNumber
    }))
        .then(response => {
            if (response.status === 202) { // ACCEPTED
                // No download wanted (file or path was only restored on server).
               return undefined;
            }
            if (!response.ok) {
                throw new Error(response.statusText);
            }
            filename = getResponseHeaderFilename(response.headers.get('Content-Disposition'));
            return response.blob();
        })
        .then(blob => {
            if (filename) {
                fileDownload(blob, filename);
            }
        })
        .catch(error => {
            console.log(error.toString());
        });
}

function FileListEntry({archiveId, entry, search, mode, changeCurrentDirectory}) {
    let path;
    if (mode === 'tree' && entry.type === 'd') {
        path = <Button color={'link'} onClick={() => changeCurrentDirectory(entry.path)}><Highlight search={search}>{entry.displayPath}</Highlight></Button>;
    } else {
        path = <Highlight search={search}>{entry.displayPath}</Highlight>;
    }
    return (
        <tr>
            <td className={'tt'}>{entry.mode}</td>
            <td className={'tt'}>{entry.mtime}</td>
            <td className={'tt'}>{humanFileSize(entry.size, true, true)}</td>
            <td className={'tt'}>
                <div className={'btn'} onClick={() => download(archiveId, entry.fileNumber)}>
                    <IconDownload/></div>
            </td>
            <td className={'tt'}>{path}</td>
        </tr>
    );
}

FileListEntry.propTypes = {
    entry: PropTypes.shape({}).isRequired,
    search: PropTypes.string,
    mode: PropTypes.string,
    changeCurrentDirectory: PropTypes.func.isRequired
};

export default FileListEntry;