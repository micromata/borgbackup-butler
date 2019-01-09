import React from 'react';
import PropTypes from 'prop-types';
import Highlight from 'react-highlighter';
import {Button, UncontrolledTooltip} from 'reactstrap';
import {IconCheck, IconDownload} from '../../general/IconComponents';
import {getResponseHeaderFilename, getRestServiceUrl, humanFileSize} from '../../../utilities/global';
import fileDownload from 'js-file-download';

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
        const entry = this.props.entry;
        let downloadArchiveId = this.props.archiveId;
        let displayPath = entry.displayPath;
        let pathCss = 'tt';

        let pathId = undefined;
        let pathTooltip = undefined;
        let pathtooltipText = undefined;

        let sizeCss = 'tt';
        let sizeTooltip = undefined;
        let sizeId = undefined;

        let modeCss = 'tt';
        let modeTooltip = undefined;
        let modeId = undefined;

        let mtimeCss = 'tt';
        let mtimeTooltip = undefined;
        let mtimeId = undefined;
        if (entry.diffStatus === 'NEW') {
            pathCss = 'tt file-new';
            pathtooltipText = 'NEW';
        } else if (entry.diffStatus === 'REMOVED') {
            pathCss = 'tt file-removed';
            // Download removed files from other archive.
            downloadArchiveId = this.props.diffArchiveId;
            pathtooltipText = 'REMOVED';
        } else if (entry.diffStatus === 'MODIFIED') {
            if (entry.differences) {
                pathCss = 'tt file-modified';
                pathtooltipText = entry.differences;
            }
            if (entry.size !== entry.diffItem.size) {
                sizeCss = 'tt file-modified';
                sizeId = `size-${entry.fileNumber}`;
                sizeTooltip =
                    <UncontrolledTooltip target={sizeId}>
                        Was: {humanFileSize(entry.diffItem.size, true, true)}
                    </UncontrolledTooltip>;
            }
            if (entry.mode !== entry.diffItem.mode) {
                modeCss = 'tt file-modified';
                modeId = `mode-${entry.fileNumber}`;
                modeTooltip =
                    <UncontrolledTooltip target={modeId}>
                        Was: {entry.diffItem.mode}
                    </UncontrolledTooltip>;
            }
            if (entry.mtime !== entry.diffItem.mtime) {
                mtimeCss = 'tt file-modified';
                mtimeId = `mtime-${entry.fileNumber}`;
                mtimeTooltip =
                    <UncontrolledTooltip target={mtimeId}>
                        Was: {entry.diffItem.mtime}
                    </UncontrolledTooltip>;
            }
        }
        if (pathtooltipText) {
            pathId = `path-${entry.fileNumber}`;
            pathTooltip =
                <UncontrolledTooltip target={pathId}>
                    {pathtooltipText}
                </UncontrolledTooltip>;
        }
        let path;
        if (this.props.mode === 'tree' && entry.type === 'd') {
            path = <Button color={'link'} onClick={() => this.props.changeCurrentDirectory(entry.path)}><Highlight
                search={this.props.search} id={pathId}>{displayPath}</Highlight></Button>;
        } else {
            path = <Highlight search={this.props.search} id={pathId}>{displayPath}</Highlight>;
        }
        let icon = entry.fileNumber >= 0 ? (this.state.downloaded ? <IconCheck/> :
            <div className={'btn'} onClick={() => this.download(downloadArchiveId, entry.fileNumber)}>
                <IconDownload/></div>) : '';
        return (
            <tr>
                <td className={pathCss}>
                    {path}{pathTooltip}
                </td>
                <td className={'tt'} style={{textAlign: 'center'}}>
                    {icon}
                </td>
                <td className={sizeCss} style={{textAlign: 'center'}}>
                    <span id={sizeId}>{humanFileSize(entry.size, true, true)}</span>{sizeTooltip}
                </td>
                <td className={modeCss}>
                    <span id={modeId}>{entry.mode}</span>{modeTooltip}
                </td>
                <td className={mtimeCss}>
                    <span id={mtimeId}>{entry.mtime}</span>{mtimeTooltip}
                </td>
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