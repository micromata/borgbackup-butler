import React from 'react';
import PropTypes from 'prop-types';
import {FormButton, FormInput, FormLabel, FormSelect, FormOption} from '../../general/forms/FormComponents';
import {IconRefresh} from '../../general/IconComponents';
import I18n from '../../general/translation/I18n';

function FileListFilters({loadLog, changeFilter, filters}) {

    return (
        <form
            onSubmit={loadLog}
            className={'form-inline'}
        >
            <FormLabel length={1}>
                Filter:
            </FormLabel>

            <FormInput
                value={filters.search}
                name={'search'}
                onChange={changeFilter}
                fieldLength={5}
            />

            <FormSelect
                value={filters.maxSize}
                name={'maxSize'}
                onChange={changeFilter}
                hint={<I18n name={'common.limitsResultSize'} />}
            >
                <FormOption value={'50'} />
                <FormOption value={'100'} />
                <FormOption value={'500'} />
                <FormOption value={'1000'} />
                <FormOption value={'10000'} />
            </FormSelect>
            <FormButton type={'submit'} bsStyle={'primary'}>
                <IconRefresh/>
            </FormButton>
        </form>
    );
}

FileListFilters.propTypes = {
    changeFilter: PropTypes.func.isRequired,
    filters: PropTypes.shape({
        search: PropTypes.string,
        maxSize: PropTypes.oneOf(['50', '100', '500', '1000', '10000']),
    }).isRequired,
    loadLog: PropTypes.func.isRequired
};

export default FileListFilters;
