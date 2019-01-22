import React from 'react';
import {Link, Route} from 'react-router-dom';
import {BreadcrumbItem} from 'reactstrap';

function BreadcrumbPath({match}) {
    return (
        <React.Fragment>
            <BreadcrumbItem>
                <Link to={`${match.url}/`}>
                    {match.params.path || 'Top'}
                </Link>
            </BreadcrumbItem>
            <Route
                path={`${match.url}/:path`}
                component={BreadcrumbPath}
                /*
                render={props =>
                    <BreadcrumbPath {...props} match={match} changeCurrentDirectory={changeCurrentDirectory} />
                }
                */
            />
        </React.Fragment>
    )
}

export default BreadcrumbPath;
