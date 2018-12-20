import React from 'react';
import PropTypes from 'prop-types';
import Highlight from 'react-highlighter';
import {IconCheck, IconDownload} from '../../general/IconComponents';
import {getResponseHeaderFilename, getRestServiceUrl, humanFileSize} from '../../../utilities/global';
import fileDownload from 'js-file-download';
import {Button} from 'reactstrap';

class FileListEntry extends React.Component {

    state = {
        downloaded: false
    };

    download(archiveId, fileNumber) {
        let filename;
        this.setState({downloaded: true});
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

    render = () => {
        let downloadArchiveId = this.props.archiveId;
        let displayPath = this.props.entry.displayPath;
        let pathCss = 'tt';
        let diffCol = undefined;
        if (this.props.entry.diffStatus === 'NEW') {
            pathCss = 'tt file-new';
            diffCol = <td className={'tt'}>NEW</td>;
        } else if (this.props.entry.diffStatus === 'REMOVED') {
            pathCss = 'tt file-removed';
            // Download removed files from other archive.
            downloadArchiveId = this.props.diffArchiveId;
            diffCol = <td className={'tt'}>REMOVED</td>;
        } else if (this.props.entry.diffStatus === 'MODIFIED') {
            pathCss = 'tt file-modified';
            diffCol = <td className={'tt'}>{this.props.entry.differences}</td>;
        }
        let path;
        if (this.props.mode === 'tree' && this.props.entry.type === 'd') {
            path = <Button color={'link'} onClick={() => this.props.changeCurrentDirectory(this.props.entry.path)}><Highlight
                search={this.props.search}>{displayPath}</Highlight></Button>;
        } else {
            path = <Highlight search={this.props.search}>{displayPath}</Highlight>;
        }
        let icon = this.state.downloaded ? <IconCheck/> :
            <div className={'btn'} onClick={() => this.download(downloadArchiveId, this.props.entry.fileNumber)}>
            <IconDownload/></div>;
        return (
            <tr>
                <td className={pathCss}>{path}</td>
                <td className={'tt'} style={{textAlign: 'center'}}>
                    {icon}
                </td>
                <td className={'tt'}>{humanFileSize(this.props.entry.size, true, true)}</td>
                <td className={'tt'}>{this.props.entry.mode}</td>
                <td className={'tt'}>{this.props.entry.mtime}</td>
                {diffCol}
            </tr>
        );
    }

    constructor(props) {
        super(props);

        this.download = this.download.bind(this);
    }
}

FileListEntry
    .propTypes = {
    entry: PropTypes.shape({}).isRequired,
    search: PropTypes.string,
    mode: PropTypes.string,
    changeCurrentDirectory: PropTypes.func.isRequired
};

export default FileListEntry;