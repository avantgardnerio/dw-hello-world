import React, {Component} from 'react';
import {connect} from 'react-redux';

import {getJobs} from "./actions";

class App extends Component {

    componentDidMount() {
        this.props.getJobs();
    }

    get jobs() {
        return this.props.jobs.map(job => <li key={job.id}>{job.name}</li>);
    }

    render() {
        return (
            <div className="App">
                <h1>Jobs</h1>
                <ul>
                    {this.jobs}
                </ul>
            </div>
        );
    }
}

const mapStateToProps = (state) => {
    return {
        jobs: state.jobs
    }
};

const mapDispatchToProps = (dispatch) => {
    return {
        getJobs: () => dispatch(getJobs())
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(App);
