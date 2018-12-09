import React from 'react';
import {Link} from 'react-router-dom';
import {Card, CardBody, CardFooter, CardHeader} from 'reactstrap';
import {formatDateTime} from "../../../utilities/global";

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
        let repoId = repo.id ? repo.id : repo.name;
        let content = [[Name, repo.name, 'name']];
        let repoText = this.buildItem(null, content);

        return <React.Fragment>
            <Card tag={Link} to={`/repos/${repo.primaryKey}`} outline color="success" className={'repo'}
                  style={{backgroundColor: '#fff'}}>
                <CardHeader>{repoId}</CardHeader>
                <CardBody>
                    <ul className="list-group list-group-flush">
                        {repoText}
                    </ul>
                </CardBody>
                <CardFooter><span className={'lastModified'}>{formatDateTime(repo.lastModified)}</span></CardFooter>
            </Card>
        </React.Fragment>
    };

    constructor(props) {
        super(props);

        this.buildItem = this.buildItem.bind(this);
    }
}

export default repoCard;
