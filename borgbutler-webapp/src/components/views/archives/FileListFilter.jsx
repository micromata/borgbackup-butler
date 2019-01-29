import React from 'react';
import {Dropdown, DropdownItem, DropdownMenu, DropdownToggle} from 'reactstrap';
import PropTypes from 'prop-types';
import {
    FormButton,
    FormCheckbox,
    FormInput,
    FormLabel,
    FormOption,
    FormSelect
} from '../../general/forms/FormComponents';
import {IconRefresh} from '../../general/IconComponents';
import I18n from '../../general/translation/I18n';

class FileListFilter extends React.Component {
    constructor(props) {
        super(props);

        this.toggle = this.toggle.bind(this);
        this.state = {
            dropdownOpen: false
        };
    }

    toggle() {
        this.setState(prevState => ({
            dropdownOpen: !prevState.dropdownOpen
        }));
    }

    render = () => {
        const archiveShortInfoList = this.props.archiveShortInfoList;
        const currentArchiveId = this.props.currentArchiveId;
        const reload = this.props.reload;
        const filter = this.props.filter;
        let archiveOptions =
            archiveShortInfoList
                .map(archive => {
                    //if (archiveId === archive)
                    let label = archive.time;
                    if (archive.fileListAlreadyCached) {
                        label = `${archive.time} ✓`
                    }
                    let disabled = undefined;
                    if (archive.id === currentArchiveId) {
                        disabled = true;
                    }
                    return <FormOption
                        value={archive.id}
                        label={label}
                        disabled={disabled}
                        key={archive.id}
                    />
                });
        return (
            <form
                onSubmit={reload}
                className={'form-inline'}
            >
                <FormLabel length={1}>
                    Filter:
                </FormLabel>

                <FormInput
                    value={filter.search}
                    name={'search'}
                    onChange={this.props.changeFilter}
                    fieldLength={7}
                    autoFocus
                    hint={'You may enter several key words separated by white spaces. Hit simply return to proceed. Example: \'borg xls !film\' searches for Excel files containing \'borg\', but not \'film\'.'}
                />
                <FormSelect
                    value={filter.diffArchiveId}
                    name={'diffArchiveId'}
                    onChange={(event) => {
                        this.props.changeFilter(event, () => reload(event))
                    }}
                    hint={'Show differences between current archive and this selected archive.'}
                >
                    <FormOption value={''} label={'Select diff archive'}/>
                    {archiveOptions}
                </FormSelect>
                <Dropdown isOpen={this.state.dropdownOpen} toggle={this.toggle} style={{paddingRight: '3pt'}}>
                    <DropdownToggle outline color="secondary" caret>
                        Settings
                    </DropdownToggle>
                    <DropdownMenu>
                        <div className={'dropdown-item'}>
                            <div className={'label'}>Mode:{' '}</div>
                            <div className={'value'}>
                                <FormSelect
                                    value={filter.mode}
                                    name={'mode'}
                                    onChange={this.props.changeFilter}
                                >
                                    <FormOption value={'tree'}/>
                                    <FormOption value={'flat'}/>
                                </FormSelect>
                            </div>
                        </div>
                        <div className={'dropdown-item'}>
                            <div className={'label'}>Result size:{' '}</div>
                            <div className={'value'}>
                                <FormSelect
                                    value={filter.maxSize}
                                    name={'maxSize'}
                                    onChange={this.props.changeFilter}
                                    hint={<I18n name={'common.limitsResultSize'}/>}
                                >
                                    <FormOption value={'50'}/>
                                    <FormOption value={'100'}/>
                                    <FormOption value={'500'}/>
                                    <FormOption value={'1000'}/>
                                    <FormOption value={'10000'}/>
                                </FormSelect>
                            </div>
                        </div>
                        <DropdownItem divider/>
                        <div className={'dropdown-item'}>
                            <FormCheckbox checked={filter.autoChangeDirectoryToLeafItem}
                                          hint={'Step automatically into single sub directories.'}
                                          name="autoChangeDirectoryToLeafItem"
                                          label={'Step automatically into sub dirs'}
                                          onChange={this.props.changeFilterCheckbox}/>
                        </div>
                        <div className={'dropdown-item'}>
                            <FormCheckbox checked={filter.openDownloads}
                                          name="openDownloads"
                                          label={'Open downloads automatically'}
                                          onChange={this.props.changeFilterCheckbox}/>
                        </div>
                    </DropdownMenu>
                </Dropdown>
                <FormButton type={'submit'} bsStyle={'primary'}>
                    <IconRefresh/>
                </FormButton>
            </form>
        );
    }
}

FileListFilter.propTypes = {
    changeFilter: PropTypes.func.isRequired,
    changeFilterCheckbox: PropTypes.func.isRequired,
    filter: PropTypes.shape({
        search: PropTypes.string,
        maxSize: PropTypes.oneOf(['50', '100', '500', '1000', '10000']),
    }).isRequired,
    reload: PropTypes.func.isRequired,
    currentArchiveId: PropTypes.string,
    archiveShortInfoList: PropTypes.array
};

export default FileListFilter;
