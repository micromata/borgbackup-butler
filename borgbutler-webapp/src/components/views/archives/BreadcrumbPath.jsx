import React from 'react';
import {Link, Route} from 'react-router-dom';
import {BreadcrumbItem} from 'reactstrap';

// Attention: Recursive Call
// https://reacttraining.com/react-router/web/example/recursive-paths
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
            />
        </React.Fragment>
    )
}

export default BreadcrumbPath;
