import React from 'react';
import {
    FormButton,
    FormField,
    FormGroup,
    FormInput,
    FormLabel,
    FormLabelInputField
} from "../../general/forms/FormComponents";
import PropTypes from "prop-types";
import I18n from '../../general/translation/I18n';
import {getRestServiceUrl} from "../../../utilities/global";
import ConfigureRepoPage from "./ConfigureRepoPage";

class RepoConfigBasePanel extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        let repoPlaceHolder = 'Enter the repo used by Borg.';
        if (this.props.remote) {
            repoPlaceHolder = 'Enter the remote path of the repo, such as user@hostname:backup.';
        }
        let repoFieldLength = 10;
        let browseButton = null;
        if (this.props.remote) {
            repoFieldLength = 9;
            browseButton = <FormButton onClick={this.browseDirectory}
                                       hint={'Browse local backup directory. (experimental!)'}><I18n
                name={'common.browse'}/>
            </FormButton>;
            repoPlaceHolder = 'Enter or browse the local path of the repo home dir used by Borg.';
        }
        return (
            <React.Fragment>
                <FormLabelInputField label={'Display name'} fieldLength={12}
                                     name={'displayName'} value={this.props.repoConfig.displayName}
                                     onChange={this.props.handleRepoConfigChange}
                                     placeholder="Enter display name (only for displaying purposes)."/>
                <FormGroup>
                    <FormLabel length={2}>{'Repo'}</FormLabel>
                    <FormField length={repoFieldLength}>
                        <FormInput
                            id={'repo'}
                            name={'repo'}
                            type={'text'}
                            value={this.props.repoConfig.repo}
                            onChange={this.props.handleRepoConfigChange}
                            placeholder={repoPlaceHolder}
                        />
                    </FormField>
                    {browseButton}
                </FormGroup>
                <FormLabelInputField label={'RSH'} fieldLength={12}
                                     name={'rsh'} value={this.props.repoConfig.rsh}
                                     onChange={this.props.handleRepoConfigChange}
                                     placeholder="Enter the rsh value (ssh command) for remote repository."
                                     className={!this.props.remote ? 'hidden' : null}/>
            </React.Fragment>
        );
    }

    browseDirectory = () => {
        const current = "&current=" + encodeURIComponent(this.props.repoConfig.repo);
        fetch(getRestServiceUrl("files/browse-local-filesystem?type=dir" + current), {
            method: "GET",
            dataType: "JSON",
            headers: {
                "Content-Type": "text/plain; charset=utf-8",
            }
        })
            .then((resp) => {
                return resp.json()
            })
            .then((data) => {
                if (data.directory) {
                    this.props.setRepoValue('repo', data.directory);
                }
            })
            .catch((error) => {
                console.log(error, "Oups, what's happened?")
            })
    }
}


RepoConfigBasePanel.propTypes = {
    handleRepoConfigChange: PropTypes.func.isRequired,
    setRepoValue: PropTypes.func.isRequired,
    repoConfig: PropTypes.object.isRequired,
    remote: PropTypes.bool.isRequired,
    editExistingRepo: PropTypes.bool
};

RepoConfigBasePanel.defaultProps = {
    editExistingRepo: false
};

export default RepoConfigBasePanel;
