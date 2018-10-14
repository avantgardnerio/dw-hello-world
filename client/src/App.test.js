import React from 'react';
import ReactDOM from 'react-dom';
import {shallow} from 'enzyme';
import sinon from 'sinon';

import App from './App';

describe('The App component', () => {
    it('renders without crashing', () => {

        // setup & exercise
        const getJobs = sinon.stub();
        const jobs = [
            {id: 1, name: 'upload'},
            {id: 2, name: 'import'}
        ];
        const wrapper = shallow(<App.WrappedComponent jobs={jobs} getJobs={getJobs} />);

        // assert
        const lis = wrapper.find('li');
        expect(lis.length).toBe(2);
        expect(getJobs.calledOnce).toBe(true);
    });
});
