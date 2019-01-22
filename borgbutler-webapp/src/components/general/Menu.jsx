import React from 'react';
import {NavLink as ReactRouterNavLink} from 'react-router-dom';
import {Collapse, Nav, Navbar, NavbarBrand, NavbarToggler, NavItem, NavLink} from 'reactstrap';
import LoadingOverlay from './loading/LoadingOverlay';
import FailedOverlay from './loading/failed/Overlay';

class Menu extends React.Component {
    getNavElement = (route, index) => {
        if (index === 0) {
            return '';
        }
        let addition = '';
        let className = '';
        // Additional Route Settings
        if (route.length >= 4) {
            if (route[3].badge) {
                addition = route[3].badge;
            }

            if (route[3].className) {
                className = route[3].className;
            }
        }
        return (
            <NavItem key={index}>
                <NavLink
                    to={route[1]}
                    tag={ReactRouterNavLink}
                    className={className}
                >
                    {route[0]} {addition}
                </NavLink>
            </NavItem>
        );
    };

    constructor(props) {
        super(props);

        this.toggle = this.toggle.bind(this);
        this.state = {
            loading: false,
            failed: false,
            isOpen: false
        };
    }

     toggle() {
        this.setState({
            isOpen: !this.state.isOpen
        });
    }

    render() {
        return (
            <Navbar className={'fixed-top'} color="light" light expand="lg">
                <NavbarBrand to="/" tag={ReactRouterNavLink}>
                    <img alt={'BorgButler logo'} src={'/images/merlin-icon.png'} width={'50px'} />BorgButler
                </NavbarBrand>
                <NavbarToggler onClick={this.toggle}/>
                <Collapse isOpen={this.state.isOpen} navbar>
                    <Nav className="ml-auto" navbar>
                        {
                            this.props.routes.map(this.getNavElement)
                        }
                    </Nav>
                </Collapse>
                <LoadingOverlay active={this.state.loading} />
                <FailedOverlay
                    title={'File Upload failed'}
                    text={this.state.failed}
                    closeModal={() => this.setState({failed: false})}
                    active={this.state.failed}
                />
            </Navbar>
        );
    }
}

export default Menu;
