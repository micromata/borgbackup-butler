import React from 'react';
import PropTypes from 'prop-types';
import {FormButton, FormInput, FormLabel, FormOption, FormSelect} from '../../general/forms/FormComponents';
import {IconRefresh} from '../../general/IconComponents';
import I18n from '../../general/translation/I18n';

function FileListFilter({reload, changeFilter, filter, currentArchiveId, archiveShortInfoList}) {
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
                onChange={changeFilter}
                fieldLength={5}
                autoFocus
                hint={'You may enter several key words separated by white spaces. Hit simply return to proceed. Example: \'borg xls !film\' searches for Excel files containing \'borg\', but not \'film\'.'}
            />

            <FormSelect
                value={filter.mode}
                name={'mode'}
                onChange={changeFilter}
            >
                <FormOption value={'tree'}/>
                <FormOption value={'flat'}/>
            </FormSelect>
            <FormSelect
                value={filter.maxSize}
                name={'maxSize'}
                onChange={changeFilter}
                hint={<I18n name={'common.limitsResultSize'}/>}
            >
                <FormOption value={'50'}/>
                <FormOption value={'100'}/>
                <FormOption value={'500'}/>
                <FormOption value={'1000'}/>
                <FormOption value={'10000'}/>
            </FormSelect>
            <FormSelect
                value={filter.diffArchiveId}
                name={'diffArchiveId'}
                onChange={(event) => {changeFilter(event); reload(event)}}
                hint={'Show differences between current archive and this selected archive.'}
            >
                <FormOption value={''} label={'Select diff archive'}/>
                {archiveOptions}
            </FormSelect>
            <FormButton type={'submit'} bsStyle={'primary'}>
                <IconRefresh/>
            </FormButton>
        </form>
    );
}

FileListFilter.propTypes = {
    changeFilter: PropTypes.func.isRequired,
    filter: PropTypes.shape({
        search: PropTypes.string,
        maxSize: PropTypes.oneOf(['50', '100', '500', '1000', '10000']),
    }).isRequired,
    reload: PropTypes.func.isRequired,
    currentArchiveId: PropTypes.string,
    archiveShortInfoList: PropTypes.array
};

export default FileListFilter;
