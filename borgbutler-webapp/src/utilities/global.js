// Later Webpack, Context etc. should be used instead.

export const isDevelopmentMode = () => {
    return process.env.NODE_ENV === 'development';
}

global.testserver = 'http://localhost:9042';
global.restBaseUrl = (isDevelopmentMode() ? global.testserver : '') + '/rest';

// Remove registered server workers from Merlin version <= V0.5 and get rid of caching hell:
if (window.navigator && navigator.serviceWorker) {
    navigator.serviceWorker.getRegistrations()
        .then(function (registrations) {
            for (let registration of registrations) {
                console.log('Found serviceWorker registration.');
                registration.unregister();
            }
        });
}


const createQueryParams = params =>
    Object.keys(params)
        .map(k => `${k}=${encodeURI(params[k])}`)
        .join('&');

export const getRestServiceUrl = (restService, params) => {
    if (params) return `${global.restBaseUrl}/${restService}?${createQueryParams(params)}`;
    return `${global.restBaseUrl}/${restService}`;
}

export const getResponseHeaderFilename = contentDisposition => {
    const matches = /filename[^;=\n]*=(UTF-8(['"]*))?(.*)/.exec(contentDisposition);
    return matches && matches.length >= 3 && matches[3] ? decodeURI(matches[3].replace(/['"]/g, '')) : 'download';
};

export const formatDateTime = (millis) => {
    const options = {year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'};
    const date = new Date(millis);
    return date.toLocaleDateString(options) + ' ' + date.toLocaleTimeString(options);
    //return date.toLocaleDateString("de-DE", options);
}

export const humanFileSize = (size, hideZero, suppressSeparatorChar) => {
    if (size == null || isNaN(size)) return '';
    if (size === 0) {
        if (hideZero) {
            return '';
        }
        return '0';
    }
    const sepChar = suppressSeparatorChar ? '' : ' ';
    const i = Math.floor(Math.log(size) / Math.log(1024));
    const amount = size / Math.pow(1024, i);
    const digits = amount < 10 ? 2 : (amount < 100 ? 1 : 0);
    return amount.toLocaleString(undefined, {maximumFractionDigits: digits}) + sepChar + ['', 'kB', 'MB', 'GB', 'TB'][i];
}

export const humanSeconds = (secondsValue) => {
    //var sec_num = parseInt(secondsValue, 10); // don't forget the second param
    let hours = Math.floor(secondsValue / 3600);
    let minutes = Math.floor((secondsValue - (hours * 3600)) / 60);
    let seconds = secondsValue - (hours * 3600) - (minutes * 60);

    if (hours < 10) {
        hours = "0" + hours;
    }
    if (minutes < 10) {
        minutes = "0" + minutes;
    }
    const secondsPrefix = seconds < 10 ? '0' : '';
    return hours + ':' + minutes + ':' + secondsPrefix + seconds.toLocaleString(undefined, {maximumFractionDigits: 0});
}

export const revisedRandId = () => Math.random().toString(36).replace(/[^a-z]+/g, '').substr(2, 10);

/* Checks if a given array is definied and is not empty. */
export const arrayNotEmpty = (array) => {
    return array && array.length;
}
