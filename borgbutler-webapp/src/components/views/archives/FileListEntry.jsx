import React from 'react';
import PropTypes from 'prop-types';
import Highlight from 'react-highlighter';
import {Button, UncontrolledTooltip} from 'reactstrap';
import {IconBan, IconCheck, IconDownload} from '../../general/IconComponents';
import {getResponseHeaderFilename, getRestServiceUrl, humanFileSize} from '../../../utilities/global';
import fileDownload from 'js-file-download';

class FileListEntry extends React.Component {

    state = {
        thisDownloaded: false,
        otherDownloaded: false
    };

    download(archiveId, fileNumber, thisDownload) {
        let filename;
        if (thisDownload) {
            this.setState({thisDownloaded: true});
        } else {
            this.setState({otherDownloaded: true});
        }
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
        let iconBan = <div className={'btn'}><IconBan/></div>;
        let iconCheck = <div className={'btn'}><IconCheck/></div>;

        let icon1 = iconCheck;
        let icon1Tooltip = '';
        if (!this.state.thisDownloaded) {
            const icon1Id = `icon1-${entry.fileNumber}`;
            icon1 = <div id={icon1Id} className={'btn'}
                         onClick={() => this.download(this.props.archive.id, entry.fileNumber, true)}>
                <IconDownload/></div>;
            icon1Tooltip = <UncontrolledTooltip target={icon1Id}>
                {this.props.archive.time}
            </UncontrolledTooltip>;
        }
        let icon2 = '';
        let icon2Tooltip = '';
        if (this.props.diffArchiveId) {
            icon2 = iconCheck;
            if (!this.state.otherDownloaded) {
                const icon2Id = `icon2-${entry.fileNumber}`;
                icon2 =
                    <div id={icon2Id} className={'btn'}
                         onClick={() => this.download(this.props.diffArchiveId, entry.fileNumber, false)}>
                        <IconDownload/></div>;
                icon2Tooltip = <UncontrolledTooltip target={icon2Id}>
                    other
                </UncontrolledTooltip>;
            }
        }
        if (entry.diffStatus === 'NEW') {
            pathCss = 'tt file-new';
            pathtooltipText = 'NEW';
            icon2 = iconBan;
            icon2Tooltip = '';
        } else if (entry.diffStatus === 'REMOVED') {
            pathCss = 'tt file-removed';
            // Download removed files from other archive.
            pathtooltipText = 'REMOVED';
            icon1 = iconBan;
            icon1Tooltip = '';
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
        return (
            <tr>
                <td className={pathCss}>
                    {path}{pathTooltip}
                </td>
                <td className={'tt'} style={{textAlign: 'center'}}>
                    {icon1}{icon1Tooltip} {icon2}{icon2Tooltip}
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
    diffArchiveId: PropTypes.string,
    changeCurrentDirectory: PropTypes.func.isRequired
};

export default FileListEntry;