import React from 'react';
import {Alert} from 'reactstrap';
import {FormButton, FormField, FormGroup, FormLabel} from '../../general/forms/FormComponents';
import PropTypes from "prop-types";
import {getRestServiceUrl} from "../../../utilities/global";
import ErrorAlert from "../../general/ErrorAlert";

class RepoConfigTestPanel extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            testStatus: undefined,
            testResult: undefined
        };
        this.onTest = this.onTest.bind(this);
    }

    onTest(event) {
        this.setState({
            testStatus: 'fetching',
            testResult: undefined
        });
        fetch(getRestServiceUrl("repoConfig/check"), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(this.props.repoConfig)
        })
            .then(response => response.text())
            .then(result => {
                //console.log("test-result: " + result);
                this.setState({
                    testStatus: result === 'OK' ? 'OK' : 'error',
                    testResult: result
                });
            })
            .catch((error) => {
                console.log("error", error);
                this.setState({
                    testStatus: 'exception',
                    testResult: undefined
                });
            });
    }

    render() {
        let testButtonColor = 'outline-info';
        if (this.state.testStatus === 'OK') {
            testButtonColor = 'outline-success';
        } else if (this.state.testStatus === 'error' || this.state.testStatus === 'exception' || this.props.repoError) {
            testButtonColor = 'outline-danger';
        } else {
            testButtonColor = 'outline-info';
        }
        let testResult = undefined;
        if (!this.state.testStatus) {
            // No test available.
        } else if (this.state.testStatus === 'exception') {
            testResult = <ErrorAlert title={'Unknown error'} description={'Internal error while calling Rest API.'}/>;
        } else if (this.state.testStatus === 'OK') {
            testResult = <Alert color={'success'}>
                OK
            </Alert>;
        } else if (this.state.testStatus === 'fetching') {
            testResult = <Alert color={'warning'}>
                Testing...
            </Alert>;
        } else {
            testResult = <ErrorAlert
                title={'Error while testing repo configuration'}
                description={this.state.testResult}
            />
        }
        return <FormGroup>
            <FormLabel length={2}>
            </FormLabel>
            <FormField length={10}>
                <FormButton onClick={this.onTest} disabled={this.state.testStatus === 'fetching'}
                            bsStyle={testButtonColor}
                            hint={'Tries to connect to the repo and to get info from.'}>Test
                </FormButton>
                {testResult}
            </FormField>
        </FormGroup>;
    }
}

RepoConfigTestPanel.propTypes = {
    repoConfig: PropTypes.object.isRequired,
    repoError: PropTypes.bool
};

RepoConfigTestPanel.defaultProps = {
    repoError: false
};

export default RepoConfigTestPanel;
