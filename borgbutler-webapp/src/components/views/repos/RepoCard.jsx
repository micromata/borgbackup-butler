import React from 'react';
import {Link} from 'react-router-dom';
import {Card, CardBody, CardFooter, CardHeader} from 'reactstrap';

class RepoCard extends React.Component {

    buildItem = (label, content) => {
        return <li className="list-group-item">{label}{content.map((line, index) => {
            return <div className="card-list-entry" key={index}>{line[0]} <span
                className={`card-list-entry-value ${line[2]}`}>{line[1]}</span>
            </div>;
        })}</li>;
    }

    render = () => {
        const repo = this.props.repo;
        let content = [['Id', repo.id, 'id'], ['Location', repo.location, 'location']];
        let repoText = this.buildItem(null, content);

        return <React.Fragment>
            <Card tag={Link} to={`/repoArchives/${repo.id}/${repo.displayName}`} outline color="success" className={'repo'}
                  style={{backgroundColor: '#fff'}}>
                <CardHeader>{repo.displayName}</CardHeader>
                <CardBody>
                    <ul className="list-group list-group-flush">
                        {repoText}
                    </ul>
                </CardBody>
                <CardFooter><span className={'lastModified'}>Last refresh: {repo.lastCacheRefresh}, last modified: {repo.lastModified}</span></CardFooter>
            </Card>
        </React.Fragment>
    };

    constructor(props) {
        super(props);

        this.buildItem = this.buildItem.bind(this);
    }
}

export default RepoCard;
