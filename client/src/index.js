import React from 'react';
import ReactDOM from 'react-dom';
import {createStore} from 'redux';
import {Provider} from 'react-redux';

import './index.css';
import App from './App';

const initialState = {
    jobs: [
        {id: 1, name: 'test'}
    ]
};
const reducer = (state = initialState, action) => {
    console.log('reducer', state, action);
    return state;
};

const store = createStore(reducer);

ReactDOM.render(
    <Provider store={store}>
        <App/>
    </Provider>
    , document.getElementById('root')
);
