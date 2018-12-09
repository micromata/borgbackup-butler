import {combineReducers} from 'redux';
import log from './log';
import version from './version';

const reducers = combineReducers({
    log,
    version
});

export default reducers;