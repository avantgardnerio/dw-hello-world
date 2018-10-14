import React, {Component} from 'react';
import {connect} from 'react-redux';

class App extends Component {

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

export default connect(mapStateToProps)(App);
